# this dockerfile simulates a crashing container
FROM alpine:latest
RUN apk add --no-cache curl
CMD ["sh", "-c", "echo 'Simulating a crash'; exit 1"]