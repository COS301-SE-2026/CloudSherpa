import http from 'k6/http';
import { config } from './config.js';

const credentials = {
  email: 'nfr-test-user@nfr-test.com',
  password: 'nfr-test-pass@123!'
}

export function loginSetup() {
    const res =  http.post(`${config.baseUrl}/auth/login`, JSON.stringify(credentials), config.basicJsonHeaderParams);

    return {
        authToken: res.cookies['auth_token'][0].value
    }
}
