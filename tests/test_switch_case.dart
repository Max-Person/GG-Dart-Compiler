void main(){
    int num = 8;
    switch(num){ // с defualt
             
        case 1: 
            print("число равно 1");
            break;
        case 8: 
            print("число равно 8");
            num++;
            break;
        case 9: 
            print("число равно 9");
            break;
        default:
            print("число не равно 1, 8, 9");
    }

    switch(num){ //без default
             
        case 1: 
            print("число равно 1");
            break;
        case 8: 
            print("число равно 8");
            num++;
            break;
        case 9: 
            print("число равно 9");
            break;
    }

    int output = 0;
    switch(num){ // одно действие сразу для нескольких блоков case подряд
     
        case 1: 
            output = 3;
            break;
        case 2: 
        case 3: 
        case 4: 
            output = 6;
            break;
        case 5: 
            output = 12;
            break;
        default:
            output = 24;
    }
}