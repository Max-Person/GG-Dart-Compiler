void main() {
  int one = 1;
  print("Type of one is ${one.runtimeType}");
  String oneInString = one.toString(); 

  print("Type of oneInString is ${oneInString.runtimeType}");
  
  String text1 = 'This is an example of a single-line string.';   
  String text2 = "This is an example of a single line string using double quotes.";   
  String text3 = """ '''This is a multiline line   
  string using the triple-quotes.   
      This is tutorial on dart strings.
  """;

  String text4 = '''This is a multiline line invalid string ''';
}
