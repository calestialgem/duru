rmdir /s /q bin
javac -d bin duru/src/module-info.java duru/src/duru/*.java
jar cfm bin/duru.jar manifest.mf -C bin .
