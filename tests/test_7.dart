void main() {
  var item = "beer";
  var price = 4.5;

  print("The price of a $item is \$${price}");

  int max = 112147483647; // too big int

  final password = 'password';
  print('The password is ${password.length > 8 ? 'OK' : 'weak'}!');

  final password = 'password';
  print('Password length is: ${((String pwd) {
        return pwd.length;
        })(password)}.');
}
