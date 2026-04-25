# Build stage
FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Run stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html
COPY --from=build /app/dist/taska-frontend/browser .
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Use a custom entrypoint to replace environment variables in config.template.json
CMD ["/bin/sh", "-c", "envsubst < config.template.json > config.json && exec nginx -g 'daemon off;'"]
