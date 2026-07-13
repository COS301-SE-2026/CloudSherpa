import apiClient from '@/lib/fetch/api-client';

export interface ThemeResponse {
  theme: 'light'|'dark';
}

export const fetchUserTheme = async (): Promise<ThemeResponse> => {
  return apiClient<ThemeResponse>('/api/preferences/theme', {
    method: 'GET',
  });
};

export const updateUserTheme = async (theme: 'light' | 'dark'): Promise<void> => {
  await apiClient<void>('/api/preferences/theme', {
    method: 'PUT',
    body: JSON.stringify({ theme }),
  });
};