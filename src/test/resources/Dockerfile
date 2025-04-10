# This Dockerfile is used to create a basic docker image for testing the dockerService functions
FROM nginx:alpine
COPY index.html /usr/share/nginx/html/
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]