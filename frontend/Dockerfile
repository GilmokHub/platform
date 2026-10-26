FROM --platform=$BUILDPLATFORM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .

# Vite build arguments
ARG VITE_API_BASE_URL
ARG VITE_DEMO_SERVER_URL
ARG VITE_GRAFANA_BASE_URL
ARG VITE_GRAFANA_DASHBOARD_UID

RUN npm run build

FROM nginxinc/nginx-unprivileged:1.27-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
USER 101:101
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
