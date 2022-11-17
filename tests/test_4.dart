class Spacecraft {
  String name;
  DateTime? launchDate;

  int? launchYear() => launchDate.year;

  Spacecraft(this.name, this.launchDate) {}

  // Named constructor that forwards to the default one.
  Spacecraft.unlaunched(String name) : this(name, null);

  // Method.
  void describe() {
    print('Spacecraft: $name');
    print(r'Unlaunched\n');
    print(r"Raw string \n\t");
  }
  
}