package jp.alhinc.kawase_hitoshi.calculate_sales;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {
	public static void main (String[] args){
		HashMap<String,String> branchNames =new HashMap<>();
		HashMap<String,Long> branchSales = new HashMap<>();
		HashMap<String,String> commodityNames = new HashMap<>();
		HashMap<String,Long> commoditySales = new HashMap<>();
		//コマンドライン引数が渡されていない場合
		//コマンドライン引数が２つ以上の場合
		if(args.length != 1 ){
			System.out.println("予期せぬエラーが発生しました");
		return;
		}
		//定義ファイル読込～値の保持メソッド
		if(!input(args[0], branchNames, branchSales, "branch.lst", "支店", "\\d{3}")){
			return;
		}
		if(!input(args[0], commodityNames, commoditySales, "commodity.lst", "商品", "[0-z]{8}")){
			return;
		}

		//売上ファイル検索～桁数連番。拡張子チェック
		File salesFiles = new File(args[0]);
		//argsからファイル名一覧をファイル型で取得
		File[] files = salesFiles.listFiles();
		ArrayList<File> rcdList = new ArrayList<>();
		//.rcdを抜いた数字のみを格納するリストを宣言
		ArrayList<String> numOnly = new ArrayList<>();
		for(int i= 0; i< files.length; i++){
			// 取得した一覧のファイルがファイルかどうかかつ、名前が８桁数字.rcdかをチェック
			if (files[i].isFile() && files[i].getName().matches("^\\d{8}.rcd$")){
				// チェックをすり抜けたファイル名をリストに入れる
				rcdList.add(files[i]);
				//.で区切って数字部分をリストに入れる
				String[] arr =  files[i].getName().split("\\.");
				numOnly.add(arr[0]);
			}
		}
		Collections.sort(numOnly);
		//数字のみのリストを最小値からカウントして歯抜けのチェック
		for(int i = 0; i < numOnly.size() - 1; i++){
			int msnX = Integer.parseInt(numOnly.get(i));
			int msnXX = Integer.parseInt(numOnly.get(i + 1));
			if(msnX + 1 != msnXX){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}
		//連番ファイルの読込
		for(int i = 0; i < rcdList.size(); i++){
			File file = new File(args[0] + File.separator + rcdList.get(i).getName());
			BufferedReader br = null;
			try {
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);
				String fir = br.readLine();
				String sec = br.readLine();
				String thi = br.readLine();
				//エラーチェック用に４行目も読み込む
				String four = br.readLine();
				//売上ファイルの行数チェック
				if(fir == null || sec == null || thi == null || four != null){
					System.out.println(rcdList.get(i).getName() + "のフォーマットが不正です");
					return;
				}
				//売上ファイルの支店コードが定義ファイルから参照できるかチェック
				if(!branchNames.containsKey(fir)){
					System.out.println(rcdList.get(i).getName() + "の支店コードが不正です");
					return;
				}
				//売上ファイルの商品コードが定義ファイルから参照できるかチェック
				if(!commodityNames.containsKey(sec)){
					System.out.println(rcdList.get(i).getName() + "の商品コードが不正です");
					return;
				}
				//売上ファイル３行目の文字列が数値であるかを判定後、LONG型に変換
				if(!thi.matches("^[0-9]+$")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
				Long thiL = Long.parseLong(thi);
				//売上ファイル３行目の桁数チェック
				if(10 < String.valueOf(thiL).length()){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				//定義ファイル読込時に設定した各売上マップに売上を計上
				Long total = branchSales.get(fir);
				Long thital = thiL + total;
				//計上後の桁数チェック
				if(10 < String.valueOf(thital).length()){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				//次は商品コードを利用して商品売上マップに計上する。
				Long total2 = commoditySales.get(sec);
				Long setal = thiL + total2;
				//計上後の桁数チェック
				if(10 < String.valueOf(setal).length()){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
				branchSales.put(fir,thital);
				commoditySales.put(sec,setal);
			}catch(IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}finally {
				if(br != null){
					try{
						br.close();
					}catch(IOException e){
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
				}
			}
		}
		if(!output(args[0], branchNames, branchSales, "branch.out")){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		if(!output(args[0], commodityNames, commoditySales, "commodity.out")){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
	}
//------------------------------------------------定義ファイルの読み込み～値の保持メソッド----------------------------------------
	static boolean input(String path, HashMap<String,String>names, HashMap<String,Long>sales, String loadFile, String definition , String terms){

		File file = new File(path, loadFile);
		BufferedReader br = null;
		//存在確認
		if(!file.exists()){
			System.out.println(definition + "定義ファイルが存在しません");
			return false;
		}
		try{
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] arr;
				arr = s.split(",");
				//definition定義ファイルのフォーマット確認(要素数が二つか、コードの表現が条件（terms）どおりか）
				if(arr.length == 2 && arr[0].matches(terms)) {
					names.put(arr[0], arr[1]);
					sales.put(arr[0], 0L);
				}else{
					System.out.println(definition + "定義ファイルのフォーマットが不正です");
					return false;
				}
			}
		}catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			if (br != null){
				try{
					br.close();
				}catch(IOException e){
					System.out.println("予期せぬエラーが発生しました");
					return false;
				}
			}
		}
		return true;
	}

//---------------------------------------------売上合算マップをソート～出力メソッド----------------------------------------------------//
	static boolean output(String path, HashMap<String,String> names,HashMap<String,Long> sales, String outputName){

		List<Map.Entry<String,Long>> sortedSales = new ArrayList<Map.Entry<String,Long>>(sales.entrySet());
		Collections.sort(sortedSales, new Comparator<Map.Entry<String,Long>>() {
			@Override
		public int compare(	Entry<String,Long> entry1, Entry<String,Long> entry2) {
			return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});
		File file = new File(path, outputName);
		BufferedWriter bw = null;
		try{
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for (Entry<String,Long> r : sortedSales) {
				bw.write(r.getKey() +  "," + names.get(r.getKey()) + "," + r.getValue());
				bw.newLine();
			}
		}catch(IOException e){
			return false;
		}finally{
			if(bw != null){
				try{
					bw.close();
				}catch(IOException e){
					return false;
				}
			}
		}
		return true;
	}
}