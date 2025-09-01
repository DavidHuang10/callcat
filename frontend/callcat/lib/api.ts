import {
    CallRequest,
    CallResponse,
    CallListResponse,
    UpdateCallRequest,
    UserResponse,
    UserPreferencesResponse,
    UpdatePreferencesRequest,
    ApiResponse,
} from "@/types";
import { API_BASE_URL } from "@/constants";

class ApiService {
    private getAuthToken(): string | null {
        return localStorage.getItem('jwt');
    }

    private async request<T>(
        endpoint: string,
        options: RequestInit = {}
    ): Promise<T> {
        const url = `${API_BASE_URL}${endpoint}`;

        const config: RequestInit = {
            headers: {
                "Content-Type": "application/json",
                ...options.headers,
            },
            ...options,
        };

        // Add Authorization header if token exists
        const token = this.getAuthToken();
        if (token) {
            config.headers = {
                ...config.headers,
                "Authorization": `Bearer ${token}`,
            };
        }

        try {
            const response = await fetch(url, config);

            if (!response.ok) {
                // Handle unauthorized - clear invalid token
                if (response.status === 401) {
                    localStorage.removeItem('jwt');
                }
                
                let errorMessage = `HTTP error! status: ${response.status}`;
                try {
                    const errorData = await response.json();
                    errorMessage = errorData.message || errorMessage;
                } catch {
                    // If can't parse JSON, use status message
                    errorMessage = `Server error (${response.status}): ${response.statusText}`;
                }
                throw new Error(errorMessage);
            }

            return await response.json();
        } catch (error) {
            console.error("API request failed:", {
                url,
                method: config.method || "GET",
                error: error instanceof Error ? error.message : error,
            });
            throw error;
        }
    }

    // Call Management
    async createCall(data: CallRequest): Promise<CallResponse> {
        return this.request<CallResponse>("/api/calls", {
            method: "POST",
            body: JSON.stringify(data),
        });
    }

    async getCalls(
        status?: string,
        limit: number = 20
    ): Promise<CallListResponse> {
        const params = new URLSearchParams();
        if (status) params.append("status", status);
        params.append("limit", limit.toString());

        return this.request<CallListResponse>(
            `/api/calls?${params.toString()}`
        );
    }

    async getCall(callId: string): Promise<CallResponse> {
        return this.request<CallResponse>(`/api/calls/${callId}`);
    }

    async updateCall(
        callId: string,
        data: UpdateCallRequest
    ): Promise<CallResponse> {
        return this.request<CallResponse>(`/api/calls/${callId}`, {
            method: "PUT",
            body: JSON.stringify(data),
        });
    }

    async deleteCall(callId: string): Promise<ApiResponse> {
        return this.request<ApiResponse>(`/api/calls/${callId}`, {
            method: "DELETE",
        });
    }

    async getCallTranscript(
        callId: string
    ): Promise<{ transcriptText: string }> {
        return this.request<{ transcriptText: string }>(
            `/api/calls/${callId}/transcript`
        );
    }

    // User Management
    async getUserProfile(): Promise<UserResponse> {
        return this.request<UserResponse>("/api/user/profile");
    }

    async updateProfile(data: Partial<UserResponse>): Promise<UserResponse> {
        return this.request<UserResponse>("/api/user/profile", {
            method: "PUT",
            body: JSON.stringify(data),
        });
    }

    async getUserPreferences(): Promise<UserPreferencesResponse> {
        return this.request<UserPreferencesResponse>("/api/user/preferences");
    }

    async updateUserPreferences(
        data: UpdatePreferencesRequest
    ): Promise<UserPreferencesResponse> {
        return this.request<UserPreferencesResponse>("/api/user/preferences", {
            method: "PUT",
            body: JSON.stringify(data),
        });
    }

    // Authentication
    async login(
        email: string,
        password: string
    ): Promise<{
        token: string;
        userId: string;
        email: string;
        fullName: string;
    }> {
        const response = await this.request<{
            token: string;
            userId: string;
            email: string;
            fullName: string;
        }>("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password }),
        });

        return response;
    }

    async logout(): Promise<void> {
        try {
            // Call backend to blacklist token (requires Authorization header)
            await this.request("/api/auth/logout", {
                method: "POST",
            });
        } catch (error) {
            console.error('Logout API call failed:', error);
            // Continue with local cleanup even if server call fails
        }

        // Always clear token from localStorage
        localStorage.removeItem('jwt');
    }

    async register(
        email: string,
        password: string,
        firstName: string,
        lastName: string
    ): Promise<{
        token: string;
        userId: string;
        email: string;
        fullName: string;
    }> {
        const response = await this.request<{
            token: string;
            userId: string;
            email: string;
            fullName: string;
        }>("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({ email, password, firstName, lastName }),
        });

        return response;
    }

    // Email Verification
    async sendVerification(email: string): Promise<ApiResponse> {
        return this.request<ApiResponse>("/api/auth/send-verification", {
            method: "POST",
            body: JSON.stringify({ email }),
        });
    }

    async verifyEmail(email: string, code: string): Promise<ApiResponse> {
        return this.request<ApiResponse>("/api/auth/verify-email", {
            method: "POST",
            body: JSON.stringify({ email, code }),
        });
    }

    // Password Reset
    async forgotPassword(email: string): Promise<ApiResponse> {
        return this.request<ApiResponse>("/api/auth/forgot-password", {
            method: "POST",
            body: JSON.stringify({ email }),
        });
    }

    async resetPassword(
        token: string,
        newPassword: string
    ): Promise<ApiResponse> {
        return this.request<ApiResponse>("/api/auth/reset-password", {
            method: "POST",
            body: JSON.stringify({ token, newPassword }),
        });
    }

    async changePassword(
        currentPassword: string,
        newPassword: string
    ): Promise<ApiResponse> {
        return this.request<ApiResponse>("/api/user/change-password", {
            method: "POST",
            body: JSON.stringify({ currentPassword, newPassword }),
        });
    }
}

export const apiService = new ApiService();
export default apiService;
