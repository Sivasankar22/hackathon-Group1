FROM node:20 AS build

# Create app directory and set ownership to non-root user "node"
RUN mkdir -p /app && chown -R node:node /app

WORKDIR /app

# Run dependency install as root, copying the package files first
COPY package*.json ./
RUN npm install --legacy-peer-deps

# Switch to non-root user for copying and building app
USER node
COPY --chown=node:node . .

# Run the build using node as non-root user
RUN node node_modules/vite/bin/vite.js build

# Stage 2 remains same (nginx serving, no permission issue as nginx runs as root or proper user)
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

