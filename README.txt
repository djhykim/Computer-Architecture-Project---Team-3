ASSEMBLER PROJECT
=================
This package contains an assembler for the C6461 architecture.

CONTENTS:
---------
- Assembler.jar       : Executable JAR file
- Assembler.java      : Main assembler source code
- source.txt          : Sample assembly input file
- listing.txt         : Sample listing file output
- load.txt            : Sample load file output
- DesignNotes.pdf     : Design document
- UserGuide.pdf	      : User Guide document

QUICK START:
------------
1. Ensure Java is installed (JDK 21 or higher)
2. Place your assembly source code in source.txt
3. Run: java -jar Assembler.jar
4. Check output files: listing.txt and load.txt

REQUIREMENTS:
-------------
- Java Development Kit (JDK) 21 or higher
- Input file: source.txt (hardcoded filename)

FEATURES:
---------
- Two-pass assembler design
- Label resolution and forward references
- Supports 5 instructions: LDR, LDA, LDX, JZ, HLT
- Supports directives: LOC, DATA, End:
- Octal output format matching C6461 specification
- Listing file with addresses, machine code and source
- Load file ready for C6461 simulator input

Team 3 -  Assembler Project