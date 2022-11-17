void main(List<String> arguments) {

  if (year >= 2001) {
    print('21st century');
  } else if (year >= 1901) {
    print('20th century'); // This is a normal, one-line comment
  }

  for (final object in flybyObjects) {
    print(object);  
  }

  for (int month = 1; month <= 12; month++) {
    print(month);
  }

  while (year < 2016) {
    year += 1;
  }
  /* Comments like these are also supported. */

  /* Comments like these are also supported. 
    /*
    Comment in comment.
    */
  */
}