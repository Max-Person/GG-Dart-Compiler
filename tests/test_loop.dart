void main()
{
    for (int i = 0; i < 5; i++) {
        print('GeeksForGeeks');
    }

    var GeeksForGeeks = [ 1, 2, 3, 4, 5 ];
    for (int i in GeeksForGeeks) {
        print(i);
    }

    int i = 1;
    for (; ;){
        print("Квадрат числа $i равен ${i *i} \n");
    }

    for (; i<9;){
        print("Квадрат числа $i равен ${i *i} \n");
        i++;
    }

    int n = 10;
    for(int i=0, j = n - 1; i < j; i++, j--){         
        print(i * j);
    }

    for (var i = 0; i < 5; i++) {
        print("!");
    }

    int j = 7;
    do{
        print(j);
        j--;
    }
    while (j > 0);

    j = 6;
    while (j > 0){
 
        print(j);
        j--;
    }

    for (int i = 0; i < 10; i++){
        if (i == 5)
            break;
        print(i);
    }

    for (int i = 0; i < 10; i++){
        if (i == 5)
            continue;
        print(i);
    }

    int c = 0;

    if(c>0){
        c++;
    }else{
        c--;
    }

    if(c<0)
        if(c>0)
            c++;
    else


    if(c>0){
    
    }else if(c <0){
    
    }

}