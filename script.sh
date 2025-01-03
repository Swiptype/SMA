# Compiler les fichiers Java
javac -d build -cp ./lib/jade.jar $(find src -name "*.java") #src/**/*.java

# Lancer JADE avec l'agent de lancement
java -cp ".:./lib/jade.jar:./build" MultiAgentLauncher
