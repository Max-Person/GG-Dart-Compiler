void main() {
	
  print('Input boolean a: ');
  var a = readBool();
  print("");

  if(a){
	print("a");
  }
  if(!a){
	print("!a");
  }
  if(a && getTrue()){
	print("a AND getTrue()");
  }
  if(a || getTrue()){
	print("a OR getTrue()");
  }
  
}

bool getTrue(){
	print("getTrue called");
	return true;
}

bool getFalse(){
	print("getFalse called");
	return false;
}