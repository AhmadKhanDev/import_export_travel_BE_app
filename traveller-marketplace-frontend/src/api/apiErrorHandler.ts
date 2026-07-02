import { type AxiosError } from "axios";

interface BackendError {
  success: false;
  message: string;
  errors?: Record<string, string[]>;
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof Error) {
    const axiosError = error as AxiosError<BackendError>;
    if (axiosError.response?.data?.message) {
      return axiosError.response.data.message;
    }
    if (axiosError.response?.status === 429) {
      return "Too many requests. Please slow down and try again.";
    }
    if (axiosError.response?.status === 403) {
      return "You don't have permission to perform this action.";
    }
    if (axiosError.response?.status === 404) {
      return "The requested resource was not found.";
    }
    if (axiosError.response?.status === 500) {
      return "Server error. Please try again later.";
    }
    return error.message;
  }
  return "An unexpected error occurred.";
}

export function getValidationErrors(
  error: unknown,
): Record<string, string> | null {
  const axiosError = error as AxiosError<BackendError>;
  const errors = axiosError?.response?.data?.errors;
  if (!errors) return null;
  const flat: Record<string, string> = {};
  Object.entries(errors).forEach(([field, msgs]) => {
    flat[field] = msgs[0];
  });
  return flat;
}
