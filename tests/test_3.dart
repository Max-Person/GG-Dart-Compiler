void main(List<String> arguments) {

  /// This is a documentation comment, used to document libraries  
  const oneSecond = Duration(seconds);
  // ···
  void printWithDelay(String message) {
    Future.delayed(oneSecond);
    print(message);
  }

  /* Comments like these are also supported. */

  /* Comments like these are also supported. 
    /*
    Invalid comment
    */
  */
}