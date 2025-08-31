export const createFormData = <T extends Record<string, any>>(data: T): FormData => {
  const formData = new FormData();
  
  Object.entries(data).forEach(([key, value]) => {
    if (value !== null && value !== undefined) {
      if (value instanceof File) {
        formData.append(key, value);
      } else if (Array.isArray(value)) {
        value.forEach((item, index) => {
          formData.append(`${key}[${index}]`, String(item));
        });
      } else {
        formData.append(key, String(value));
      }
    }
  });
  
  return formData;
};

export const validateForm = <T extends Record<string, any>>(
  data: T,
  validators: Record<keyof T, (value: any) => string | null>
): { isValid: boolean; errors: Record<keyof T, string> } => {
  const errors = {} as Record<keyof T, string>;
  
  Object.entries(validators).forEach(([field, validator]) => {
    const error = validator(data[field as keyof T]);
    if (error) {
      errors[field as keyof T] = error;
    }
  });
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
};

export const resetForm = <T extends Record<string, any>>(
  initialState: T
): T => {
  return { ...initialState };
};