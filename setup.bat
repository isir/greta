cd application
cd Modular
:: You need ANT to perform the build
ant -buildfile build_manual.xml -Dant.build.javac.target=1.8 -Dant.build.javac.source=1.8
