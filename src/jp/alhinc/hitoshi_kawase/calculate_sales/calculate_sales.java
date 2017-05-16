package jp.alhinc.hitoshi_kawase.calculate_sales;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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

public class calculate_sales {
	public static void main (String[] args){
		HashMap<String,String> branchnames =new HashMap<String, String>();
		HashMap<String,Long> branchsales = new HashMap<String, Long>();
		HashMap<String,String> commoditynames = new HashMap<String,String>();
		HashMap<String,Long> commoditysales = new HashMap<String, Long>();
		ArrayList<String> matchfiles1 = new ArrayList<String>();
		ArrayList<String> getnames = new ArrayList<String>();

	//コマンドライン引数が渡されていない場合
	//コマンドライン引数が２つ以上の場合
		if(args.length != 1 ){
		System.out.println("予期せぬエラーが発生しました");
		return;
		}
	/*支店定義ファイルの読み込み～値の保持*/
		File storefile = new File(args[0]+File.separator+"branch.lst");
		BufferedReader br = null;
		if(storefile.exists()){	//支店定義ファイルが存在しているか確認
			try{
				FileReader fr = new FileReader(storefile);
				br = new BufferedReader(fr);
				String s;
				while((s = br.readLine()) != null) {
					String[] arr;
					arr = s.split(",");
					//支店定義ファイルの要素数が１つ以下
					//支店定義ファイルの要素数が３つ以上
					if(arr.length != 2){
						System.out.println("支店定義ファイルのフォーマットが不正です");
						return;
					}
					if(arr[0].matches("\\d{3}")) {
						branchnames.put(arr[0], arr[1]);
						branchsales.put(arr[0], 0L);
					//支店コードにアルファベットが含まれている場合
					//〃　ひらがな
					//〃　カタカナ
					//〃　漢字
					//〃　記号
					//〃　桁数が２桁以下
					//〃　桁数が４桁以上
					}else{
						System.out.println("支店定義ファイルのフォーマットが不正です");
						return;
					}
				}
			}catch(IOException e) {
				System.out.println("支店定義ファイルのフォーマットが不正です。");
			}finally{
				try{
					br.close();
				}catch(IOException e){
					System.out.println("支店定義ファイルのフォーマットが不正です。");
				}
			}
			//支店定義ファイルが存在しない場合
		}else {
			System.out.println("支店定義ファイルが存在しません。");
			return;
		}
		/*商品定義ファイルの読み込み～値の保持*/
		File goodsfile = new File(args[0]+File.separator+"commodity.lst");
		BufferedReader cr= null;
		if(goodsfile.exists()){
			try{
				FileReader vr = new FileReader(goodsfile);
				cr = new BufferedReader(vr);
				String s;
				while((s = cr.readLine()) != null) {
					String[] arr;
					arr = s.split(",");
					if(arr.length == 2 && arr[0].matches("[0-z]{8}")){
						commoditynames.put(arr[0], arr[1]);
						commoditysales.put(arr[0], 0L);
					}else{
						System.out.println("商品定義ファイルのフォーマットが不正です");
						return;
					}
				}
			}catch(IOException e) {
				System.out.println("支店定義ファイルのフォーマットが不正です。");
			}finally {
				try{
					cr.close();
				}catch(IOException e){
					System.out.println("支店定義ファイルのフォーマットが不正です。");
				}
			}
		}else{
			System.out.println("商品定義ファイルが存在しません。");
			return;
		}
		//売上ファイル検索～桁数連番　拡張子チェック
		File sales = new File(args[0]);
		File[] files = sales.listFiles();//argsからファイルを取得
		ArrayList<File> rcdList = new ArrayList<>();

		for(int i= 0; i< files.length; i++){
			if (files[i].isFile() && files[i].getName().matches("^\\d{8}.rcd$")){
				// チェックをすり抜けたリストに入れる
				rcdList.add(files[i]);
				String[] arr =  files[i].getName().split("\\.");
				matchfiles1.add(arr[0]);
			}
		}
		Collections.sort(matchfiles1);

		for(int cnt =0; cnt< matchfiles1.size()-1; cnt++){//最小値からカウントして歯抜けのチェック
			int msnX = Integer.parseInt(matchfiles1.get(cnt));
			int msnXX = Integer.parseInt(matchfiles1.get(cnt + 1));
			if(msnX+1 != msnXX){				//msnXX-msnX=＝1 という方法でも可能
				System.out.println("売上ファイル名が連番になっていません。");
				return;
			}
		}

		for(int fc = 0; fc<matchfiles1.size(); fc++){
			File matchfile = new File(args[0]+File.separator+matchfiles1.get(fc)+ ".rcd");
			BufferedReader xr = null;
				try {
					FileReader vr = new FileReader(matchfile);
					xr = new BufferedReader(vr);


					String fir = xr.readLine();
					String sec =xr.readLine();
					String thi  = xr.readLine();
					String four = xr.readLine();

					if(branchnames.get(fir) == null || commoditynames.get(sec) == null){
						System.out.println(matchfiles1.get(fc) + ".rcdのフォーマットが不正です。");
						return;
					}




					if(thi == null || four != null){
						System.out.println(matchfiles1.get(fc)+".rcdのフォーマットが不正です");
						return;
					}
					//３行目の数字文字列をLONG型に変換
					Long thiL = Long.parseLong(thi);

					if(10 < String.valueOf(thiL).length()){
						System.out.println("合計金額が１０桁を超えました");
						return;
					}

					Long total = branchsales.get(fir);
					Long thital = thiL + total;
					branchsales.put(fir,thital);
					if(10 < String.valueOf(branchsales.get(fir)).length()){
						System.out.println("合計金額が１０桁を超えました");
						return;
					}
					//次は商品コードを利用して商品売上goodssaleマップに合算する。
					Long total2 = commoditysales.get(sec);
					Long setal = thiL + total2;
					commoditysales.put(sec,setal);
					if(10 < String.valueOf(commoditysales.get(sec)).length()){
						System.out.println("合計金額が１０桁を超えました");
						return;
					}
				}catch(IOException e) {
					System.out.println("例外処理の発生");
				}finally {
					try{
						xr.close();
					}catch(IOException e){
						System.out.println();
					}
				}
		}

									//matchfilist1のリストには今8桁の番号が格納されてる....。
/* 支店別集計ファイルの出力	保持した各支店の総売り上げ、商品の総売り上げをソート*/
		//支店 sss（sortedstoresale）List 生成 (ソート用)
		List<Map.Entry<String,Long>> sss= new ArrayList<Map.Entry<String,Long>>(branchsales.entrySet());
		Collections.sort(sss, new Comparator<Map.Entry<String,Long>>() {
			@Override
			public int compare(	Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
		//このリストには要素の中にマップが入ってる（キーと値）
		//ファイル出力の処理
		File stob = new File(args[0]+File.separator+"baranch.out");
		BufferedWriter bw = null;
		try{
			FileWriter fw = new FileWriter(stob);
			bw = new BufferedWriter(fw);
			for (Entry<String,Long> s : sss) {
				bw.write(s.getKey()+ "," +branchnames.get(s.getKey()) + "," + s.getValue() + "\r\n");
			}
		}catch(FileNotFoundException m){
			System.out.println("予期せぬエラーが発生しました1");
			return;
		}catch(IOException e){
				System.out.println(e);
		}finally{
			try{
				bw.close();
			}catch(IOException e){
				System.out.println(e);
			}
		}
/* 商品別集計ファイルの出力（保持した各商品の総売り上げ、*/
		List<Map.Entry<String,Long>> sgs= new ArrayList<Map.Entry<String,Long>>(commoditysales.entrySet());
		Collections.sort(sgs, new Comparator<Map.Entry<String,Long>>() {
			@Override
			public int compare(	Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
		//このリストには要素の中にマップが入ってる（キーと値）
		//ファイル出力の処理
		File goob = new File(args[0]+File.separator+"commodity.out");
		BufferedWriter tr = null;
			try{
				FileWriter fw = new FileWriter(goob);
				tr = new BufferedWriter(fw);
				for (Entry<String,Long> s : sgs) {
					tr.write(s.getKey()+ "," + commoditynames.get(s.getKey())+ "," + s.getValue() +"\r\n");
				}
			}catch(FileNotFoundException m){
					System.out.println("予期せぬエラーが発生しました。");
					return;
			}catch(IOException e){
					System.out.println(e);


			}finally{
				try{
					tr.close();
				}catch(IOException e){
					System.out.println(e);
				}
			}
	}
}