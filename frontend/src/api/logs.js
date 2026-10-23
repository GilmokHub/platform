import { api } from './client';

export const fetchRecentLogs = async () => {
    const response = await api.get('/gilmok-platform/admin/logs');
    return Array.isArray(response) ? response : [];
};