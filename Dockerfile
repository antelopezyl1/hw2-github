# Dockerfile - run the OpenAPI mock with Prism
FROM node:20-alpine
RUN npm i -g @stoplight/prism-cli
WORKDIR /work
COPY openapi.yml /work/openapi.yml
EXPOSE 8080
# Listen on all interfaces at port 8080
CMD ["prism","mock","-h","0.0.0.0","-p","8080","/work/openapi.yml"]
