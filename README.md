# NiCadJimple

### Requirments
- Install [Txl and Turing+ Compiler ](https://www.txl.ca/txl-index.html) 
- java-1.8.0-openjdk-amd64


### How to use:

- 1. Get a fresh copy of BigCloneBench-files and H2.db in the [stubbed version](https://github.com/StoneDetector/Stubber)
	- 1.1 Put the java-files (including the folders) in the "Testfiles" folder and the Datebase in BigCloneEval/bigclonebenchdb
- 2. create the mapper
    - 2.1 run `./create_mapper Testfiles` which will create mapper.csv 
        - for BIG-DATA (100MB+) I recommand using the splited versions
        - `./create_mapper_first Testfiles` generates the mapper_java.csv which provides all function names 
        - `./create_mapper_second Testfiles` generates the jimple files
        - `./create_mapper_third Testfiles` generates mapper.csv 
    - 2.2 this also replaces! all java and jar files in Testfiles with their respective jimple-files (if you still need the original files you should create a Backup beforehand )
    - 2.3 move all folders (or if there are non, files) into BigCloneEval/ijadataset/bcb_reduced 
- 3. Ready up BigCloneEval (BCE)
    - 3.1 go into BCE and run `make` 
    - 3.2 go into commands/ folder and do run `./init`
    - 3.3 run `./registerTool -n "NiCad" -d "Default Configuration"`
        - in sample/nicadRunner the path to NiCad is hardcoded, you should change this to your path
    - 3.4 run `./clearClones -t1`
    - 3.5 run `./detectClones -m=5000 -o=../output/output_bce.csv -r ../sample/nicadRunner`
    - this creates the output_bce.csv in output folder
- 4. Map output.csv to mapper.csv
    - move output_bce.csv in the output folder (NiCadJimple/output)
    - run `./create_clones.sh` in NiCadJimple
    - in BigCloneEval/commands folder
        - `./importClones -t1 -c output.csv`
        - `./evaluateTool -t=1 --output="report" --mil=15`
        - this creates a the report for the created clones