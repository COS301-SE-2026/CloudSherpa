const BASE_URL = __ENV.BASE_URL ?? 'http://host.docker.internal:8083';

const params = {
    headers: {
        'Content-Type': 'application/json',
    },
};

export const config = {
    baseUrl: BASE_URL,
    basicJsonHeaderParams: params
}