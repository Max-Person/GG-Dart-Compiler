abstract class Describable {
  void describe();

  void describeWithEmphasis() {
    print('=========');
    describe();
    print('=========');
  }
}

class APIConstant {
  static RequestKeys requestKeys = const RequestKeys();
  static ResponseKeys responseKeys = const ResponseKeys();

  static const String baseUrl = 'Your Project base url';
}

class RequestKeys {
  const RequestKeys();
  String email() => 'email';
  String password() => 'password';
}

class ResponseKeys {
  const ResponseKeys();
  String data() => 'data';
  String status() => 'status';
}