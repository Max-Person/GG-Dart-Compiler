import 'package:cli/cli.dart' as cli;

void main( arguments) {

  /// This is a documentation comment, used to document libraries  
  const oneSecond = Duration(seconds: 1);
  // ···
  Future<void> printWithDelay(String message) async {
    await Future.delayed(oneSecond);
    print(message);
  }

  /* Comments like these are also supported. */

  /* Comments like these are also supported. 
    /*
    Invalid comment
    
  */
}