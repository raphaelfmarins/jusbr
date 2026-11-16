JAVA_HOME ?= $(shell /usr/libexec/java_home -v 17 2>/dev/null)
export JAVA_HOME

.PHONY: run stop test test-backend test-frontend e2e

run:
	docker compose up --build

stop:
	docker compose down

test: test-backend test-frontend

test-backend:
	cd backend && ./gradlew test

test-frontend:
	cd frontend && npm ci --silent && npm test

e2e:
	npx -y newman@6.2.1 run docs/postman/url-shortener.postman_collection.json
