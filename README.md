# Elicitation question selection for additive value model in multiple criteria decision problems - _vfeqs_

## Prerequisites

- [Java 1.6+](https://java.com/download)
- [GLPK for Java](http://glpk-java.sourceforge.net/)
- [Maven](https://maven.apache.org/install)
- [polyrun](https://www.github.com/kciomek/polyrun) installed in your Maven local repository

## Build

Get latest version with git:

    git clone git@github.com:kciomek/vfeqs.git

Build jar with Maven:

    cd vfeqs
    mvn clean compile package

Now, you can find _target/vfeqs-0.15.0-jar-with-dependencies.jar_.

## Run

The package allows you to perform multiple tests at once. Each experiment set is described by one input line. For more information, help and description of input and output format use:

    java -jar target/vfeqs-0.15.0-jar-with-dependencies.jar -h

For instance, to run _vfeqs_ in interactive mode for ranking problem of 10 objects evaluated on 3 criteria (attributes) with assumed linear marginal value function ( _/2_ ) with performance matrix saved in _file-path.perf_ (tab-separated values) and with random elicitation question selection use:

    java -jar target/vfeqs-0.15.0-jar-with-dependencies.jar -r -i "R 0 10/3/2/file-path.perf 0 1/1/1 interactive RAND 0.0001 1000 tfl"

Note, that it is assumed that all criteria are to be maximized and objects are 0-based numbered.


