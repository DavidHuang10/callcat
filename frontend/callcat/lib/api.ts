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

const API_BASE_URL =
    process.env.NEXT_PUBLIC_API_URL || "https://api.call-cat.com";

class ApiService {
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

        // Enable credentials to include httpOnly cookies
        config.credentials = 'include';

        try {
            const response = await fetch(url, config);

            if (!response.ok) {
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
        userId: string;
        email: string;
        fullName: string;
    }> {
        const response = await this.request<{
            userId: string;
            email: string;
            fullName: string;
        }>("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ email, password }),
            credentials: 'include',
        });

        // httpOnly cookie is set by server, no client-side token storage needed
        return response;
    }

    async logout(): Promise<void> {
        await this.request("/api/auth/logout", {
            method: "POST",
            credentials: 'include',
        });

        // httpOnly cookie is cleared by server
    }

    async register(
        email: string,
        password: string,
        firstName: string,
        lastName: string
    ): Promise<{
        userId: string;
        email: string;
        fullName: string;
    }> {
        const response = await this.request<{
            userId: string;
            email: string;
            fullName: string;
        }>("/api/auth/register", {
            method: "POST",
            body: JSON.stringify({ email, password, firstName, lastName }),
            credentials: 'include',
        });

        // httpOnly cookie is set by server
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
}

export const apiService = new ApiService();
export default apiService;
