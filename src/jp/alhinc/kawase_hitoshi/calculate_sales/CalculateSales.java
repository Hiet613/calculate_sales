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
		HashMap<String,String> branchnames =new HashMap<>();
		HashMap<String,Long> branchsales = new HashMap<>();
		HashMap<String,String> commoditynames = new HashMap<>();
		HashMap<String,Long> commoditysales = new HashMap<>();
		ArrayList<String> numonly = new ArrayList<>();
		//コマンドライン引数が渡されていない場合
		//コマンドライン引数が２つ以上の場合
		if(args.length != 1 ){
		System.out.println("予期せぬエラーが発生しました");
		return;
		}
		if(!input(args[0], branchnames, branchsales, "branch.lst", "支店定義", "\\d{3}")){
			return;
		}
		if(!input(args[0], commoditynames, commoditysales, "commodity.lst", "商品定義", "[0-z]{8}")){
			return;
		}

		//売上ファイル検索～桁数連番。拡張子チェック
				File salesfiles = new File(args[0]);
				//argsからファイル名一覧をファイル型で取得
				File[] files = salesfiles.listFiles();
				ArrayList<File> rcdList = new ArrayList<>();
				for(int i= 0; i< files.length; i++){
					// 取得した一覧のファイルがファイルかどうかかつ、名前が８桁数字.rcdかをチェック
					if (files[i].isFile() && files[i].getName().matches("^\\d{8}.rcd$")){
						// チェックをすり抜けたファイル名をリストに入れる
						rcdList.add(files[i]);
						//.で区切って数字部分をリストに入れる
						String[] arr =  files[i].getName().split("\\.");
						numonly.add(arr[0]);
					}
				}
				Collections.sort(numonly);
				//数字のみのリストを最小値からカウントして歯抜けのチェック
				for(int cnt =0; cnt<numonly.size()-1; cnt++){
					int msnX = Integer.parseInt(numonly.get(cnt));
					int msnXX = Integer.parseInt(numonly.get(cnt + 1));
					if(msnX+1 != msnXX){
						System.out.println("売上ファイル名が連番になっていません");
						return;
					}
				}
				//連番ファイルの読込
				for(int fc = 0; fc<numonly.size(); fc++){
					File serial = new File(args[0]+File.separator+numonly.get(fc)+ ".rcd");
					BufferedReader brserial = null;
					try {
						FileReader vr = new FileReader(serial);
						brserial = new BufferedReader(vr);
						String fir = brserial.readLine();
						String sec =brserial.readLine();
						String thi  = brserial.readLine();
						//エラーチェック用に４行目も読み込む
						String four = brserial.readLine();
						//売上ファイルの行数チェック
						if(fir == null || sec == null ||thi == null || four != null){
							System.out.println(numonly.get(fc)+".rcdのフォーマットが不正です");
							return;
						}
						//売上ファイルの支店コードが定義ファイルから参照できるかチェック
						if(!branchnames.containsKey(fir)){
							System.out.println(numonly.get(fc) + ".rcdの支店コードが不正です");
							return;
						}
						//売上ファイルの商品コードが定義ファイルから参照できるかチェック
						if(!commoditynames.containsKey(sec)){
							System.out.println(numonly.get(fc) + ".rcdの商品コードが不正です");
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
						Long total = branchsales.get(fir);
						Long thital = thiL + total;
						branchsales.put(fir,thital);
						//計上後の桁数チェック
						if(10 < String.valueOf(branchsales.get(fir)).length()){
							System.out.println("合計金額が10桁を超えました");
							return;
						}
						//次は商品コードを利用して商品売上マップに計上する。
						Long total2 = commoditysales.get(sec);
						Long setal = thiL + total2;
						commoditysales.put(sec,setal);
						//計上後の桁数チェック
						if(10 < String.valueOf(commoditysales.get(sec)).length()){
							System.out.println("合計金額が10桁を超えました");
							return;
						}
					}catch(IOException e) {
						System.out.println("予期せぬエラーが発生しました");
						return;
					}finally {
						if(brserial != null){
							try{
								brserial.close();
							}catch(IOException e){
								System.out.println("予期せぬエラーが発生しました");
								return;
							}
						}
					}
				}
				if(!output(args[0], branchnames, branchsales, "branch.out")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
				if(!output(args[0], commoditynames, commoditysales, "commodity.out")){
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
			System.out.println(definition + "ファイルが存在しません");
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
					System.out.println(definition +"ファイルのフォーマットが不正です");
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

		List<Map.Entry<String,Long>> sortedsales = new ArrayList<Map.Entry<String,Long>>(sales.entrySet());
		Collections.sort(sortedsales, new Comparator<Map.Entry<String,Long>>() {
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
			for (Entry<String,Long> r : sortedsales) {
				bw.write(r.getKey() +  "," +names.get(r.getKey()) + "," + r.getValue());
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