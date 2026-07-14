import apiClient from '@/lib/fetch/api-client';

export interface ThemeResponse {
  theme: 'light'|'dark';
}

export const fetchUserTheme = async (): Promise<ThemeResponse> => {
  return apiClient<ThemeResponse>('/preferences/theme', {
    method: 'GET',
  });
};

export const updateUserTheme = async (theme: 'light' | 'dark'): Promise<void> => {
  await apiClient<void>('/preferences/theme', {
    method: 'POST',
    body: JSON.stringify({ theme }),
  });
};