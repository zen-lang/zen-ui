.EXPORT_ALL_VARIABLES:
.PHONY: test deploy ui 

repl:
	mkdir -p target/stylo/dev/ target/shadow/dev/
	clj -A:build:ui:test:nrepl -m nrepl.cmdline

npm:
	npm install

jar:
	clj -A:ui:build -m build

up:
	docker-compose up -d

test:
	clj -A:ui:test:kaocha

test-watch:
	clj -A:ui:test:kaocha --watch --plugin notifier --reporter documentation
