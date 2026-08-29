import http from 'k6/http';

const BASE_URL = __ENV.BASE_URL ?? 'http://host.docker.internal:8083';

const credentials = {
  email: 'nfr-test-user@nfr-test.com',
  password: 'nfr-test-pass@123!'
}

const params = {
    headers: {
        'Content-Type': 'application/json',
    },
};


export function loginSetup() {
    const res =  http.post(`${BASE_URL}/auth/login`, JSON.stringify(credentials), params);

    return {
        authToken: res.cookies['auth_token'][0].value
    }
}
