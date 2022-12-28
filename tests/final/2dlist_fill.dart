void main(){
	var list = [
		[0,0,0,0,0,0,0],
		[0,1,1,1,1,1,0],
		[0,1,0,0,0,1,0],
		[0,1,0,0,0,1,0],
		[0,1,1,1,1,1,0],
		[0,0,0,0,0,0,0]
	];
	print(list);
	fill(list, 3, 2, 2);
	print(list);


}

void fill(List<List<int>> list, int row, int col, int filler){
  	//print("row $row, col $col = ${list[row][col]}");
	int filled = list[row][col];
	int height = list.length;
	int width = list[0].length;
  	//print("width $width, height $height");
	list[row][col] = filler;
	if(row + 1 < height && list[row+1][col] == filled) fill(list, row+1, col, filler);
	if(row - 1 >= 0 	&& list[row-1][col] == filled) fill(list, row-1, col, filler);
	if(col + 1 < width 	&& list[row][col+1] == filled) fill(list, row, col+1, filler);
	if(col - 1 >= 0 	&& list[row][col-1] == filled) fill(list, row, col-1, filler);
}

List<List<int>> zeroes(int height, int width){
	List<List<int>> out = [];
	int i = 0;
	while(i < height){
		List<int> row = [];
		int j = 0;
		while(j < width){
			row.add(0);
			j++;
		}
		out.add(row);
		i++;
	}
	return out;
}
