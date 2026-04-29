FROM node:20-slim

WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy the rest of the application
COPY . .

# Expose Vite default port
EXPOSE 5173

# Run Vite with --host flag to allow access from outside the container
CMD ["npm", "run", "dev", "--", "--host"]
