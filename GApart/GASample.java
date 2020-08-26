import org.apache.commons.math3.genetics.*;
import java.util.*;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.processing.CMXController;
import jp.crestmuse.cmx.elements.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static java.util.Arrays.asList;

public class GASample {
      static List<Integer> createInitial(int part_count, int part_size, int drum_size) {
            int[] priority_num = new int[part_count];

            // 1~6までの数字を格納
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int i = 1; i <= part_count; i++) {
                  list.add(i);
            }
            // listをシャッフル
            Collections.shuffle(list);

            for (int i = 0; i < part_count; i++) {
                  priority_num[i] = list.get(i);
            }
            List<Integer> rep = new ArrayList<Integer>();

            for (int i = 0; i < part_count; i++) {
                  rep.add(priority_num[i]);
                  if (part_size >= priority_num[i]) {
                        rep.add((int) (1 + 7 * Math.random()));
                  } else {
                        rep.add((int) (8 + 4 * Math.random()));
                  }

            }

            return rep;
      }

      // 3つのなみごとに音を入れる
      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> square_list = new LinkedList();
      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> PartList = new LinkedList();

      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> tri_list = new LinkedList();
      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> noise_list = new LinkedList();
      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> Drum_PartList = new LinkedList();

      static List<Integer> Tom_Notenum = asList(41, 43, 45, 47, 48, 50);// タムのノートナンバー
      static List<Integer> Bass_Notenum = asList(36, 35);// バスドラのノートナンバー
      static List<Integer> Cymbal_Notenum = asList(49, 51, 52, 55, 57, 59);// シンバルのノートナンバー
      static List<Integer> Snare_Notenum = asList(38, 40);// スネアのノートナンバー
      static List<Integer> HiHat_Notenum = asList(42, 44, 46);// ハイハットのノートナンバー

      public static void main(String[] args) {
            try {

                  CMXController cmx = CMXController.getInstance();
                  SCCDataSet sccdataset = cmx.readSMFAsMIDIXML(args[0]).toSCCXML().toDataSet(); //
                  SCCDataSet.Part[] partlist = sccdataset.getPartList(); // パーとを取得

                  GASample gaS = new GASample();

                  // 小節数
                  int mesure = Mesure_Calculation(partlist);
                  int part_count = 1;
                  System.out.println("小節数: " + mesure);

                  int bass = in_bass(partlist);

                  PartList.add(generate_sectionList(partlist[bass].getNoteOnlyList()));
                  System.out.println("ベースパート出力" + PartList.get(0).get(0));
                  for (int i = 0; i < partlist.length; i++) {
                        System.out.println("i=" + i);
                        MutableMusicEvent[] notelist = partlist[i].getNoteOnlyList(); // 順にパートの音符を取得

                        if (bass != i && notelist.length != 0 && i != 0 && i != 9) {
                              System.out.println("i" + i);
                              square_list.add(generate_sectionList(notelist));
                              PartList.add(generate_sectionList(notelist));
                              part_count++;
                        }

                        // ドラムの処理

                        if (i == 9 && notelist.length != 0) {

                              generate_Drum_sectionList(notelist, Tom_Notenum, 1);

                              generate_Drum_sectionList(notelist, Bass_Notenum, 1);

                              generate_Drum_sectionList(notelist, Cymbal_Notenum, 2);

                              generate_Drum_sectionList(notelist, Snare_Notenum, 2);

                              generate_Drum_sectionList(notelist, HiHat_Notenum, 2);

                        }
                  }
                  // 全体のリストを出力

                  part_count += Drum_PartList.size();
                  System.out.println("矩形波出力");
                  for (int i = 1; i < PartList.size(); i++) {
                        System.out.println(PartList.get(i));

                  }
                  System.out.println("");

                  // System.out.println("PartList" + PartList);
                  System.out.println("三角波");
                  for (int i = 0; i < tri_list.size(); i++) {
                        // for (int j = 0; j < tri_list.get(i).size(); j++) {
                        System.out.println(tri_list.get(i));
                        // }

                  }
                  System.out.println("三角波サイズ" + tri_list.size());

                  System.out.println("");
                  System.out.println("ノイズ波");
                  for (int i = 0; i < noise_list.size(); i++) {
                        // for (int j = 0; j < noise_list.get(i).size(); j++) {
                        System.out.println(noise_list.get(i));
                        // }

                  }
                  System.out.println("PartList_size:" + PartList.size());
                  System.out.println("Drum_PartListsize:" + Drum_PartList.size());

                  System.out.println("partcount : " + part_count);

                  // ---------------------------------------------------------------

                  long start = System.currentTimeMillis();// currentTimeMillis();時刻

                  // GeneticAlgoithmクラスのインスタンスを作成する
                  // MyMutation(min,max) = 突然変異 UniformCrossover(0.5)50% = 交叉（一部入れ替え）
                  // それらの中から最も適した染色体を選択(2個選択)
                  GeneticAlgorithm ga = new GeneticAlgorithm(new UniformCrossover(0.5), 0.8, new MyMutation(1, 11), 0.2,
                              new TournamentSelection(2));

                  // Create many chromosomes 染色体作成
                  List<Chromosome> clist = new ArrayList<Chromosome>();

                  for (int i = 0; i < 100; i++) {
                        // // ①初期個体決める。初期個体をランダムに個体をパート数分作る
                        clist.add(new MyChromosome(createInitial(part_count, PartList.size(), Drum_PartList.size()),
                                    PartList, Drum_PartList, part_count, square_list, tri_list, noise_list));
                        // System.out.println("clidt=" + clist.get(i));
                        // System.out.println("適合度=" + clist.get(i).getFitness());
                        // System.out.println();
                  }
                  System.out.println("ok");
                  Population pop1 = new ElitisticListPopulation(clist, 1000, 0.1);
                  // １０回目の世代数で停止します
                  Population pop2 = ga.evolve(pop1, new FixedGenerationCount(10));

                  MyChromosome c = (MyChromosome) pop2.getFittestChromosome();

                  List<Integer> rep = c.getRepresentation();
                  System.out.println();
                  System.out.println("　最終的な結果 " + c.getRepresentation());
                  System.out.println("最終適合度=" + c.getFitness());
                  // 楽譜にぶち込んでいく

                  int j = 0;
                  // SCCDataSet newScc;
                  SCCDataSet.Part newPart;
                  SCCDataSet newScc = new SCCDataSet(sccdataset.getDivision());
                  SCCDataSet.Part newPart0;
                  SCCDataSet.Part newPart1;
                  SCCDataSet.Part newPart2;
                  SCCDataSet.Part newPart3;
                  SCCDataSet.Part newPart4;

                  int judge = 1;
                  // 楽譜にしていく
                  for (int i = 0; i < rep.size(); i += 2) {
                        System.out.println();
                        System.out.println();
                        System.out.println(" i= " + i + " パート " + rep.get(i) + "に");
                        System.out.println("ルール" + rep.get(i + 1) + "を適応");
                        System.out.println("入れるもの");

                        LinkedList<MutableMusicEvent> new_SEQList = new LinkedList();//
                        LinkedList<LinkedList<MutableMusicEvent>> new_NSList = new LinkedList(); // 新しい小節生成
                        LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> new_sectionList = new LinkedList();// 新しいパートのリスト
                        int emp = emptiness(rep.get(i) - 1);
                        int bass_judge;

                        if (rep.get(i) == 1) {
                              bass_judge = 1;

                        } else {
                              bass_judge = 0;
                        }

                        // もし小節が空出なかったら
                        if (emp != 1) {
                              if (rep.get(i + 1) == 1) {
                                    j++;
                                    // ルール１を適応
                                    new_SEQList = MyChromosome.rule1(rep.get(i) - 1);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);

                              } else if (rep.get(i + 1) == 2) { // ルール2を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule2(rep.get(i) - 1);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);

                              } else if (rep.get(i + 1) == 3) { // ルール3を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule3(rep.get(i) - 1);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);

                              } else if (rep.get(i + 1) == 4) { // ルール4を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule4(rep.get(i) - 1);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);

                              } else if (rep.get(i + 1) == 5) { // ルール5を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule5(rep.get(i) - 1);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);
                              } else if (rep.get(i + 1) == 6) { // ルール6を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule6(rep.get(i) - 1);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);
                              } else if (rep.get(i + 1) == 7) { // ルール7を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule7(rep.get(i) - 1);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);
                              } else if (rep.get(i + 1) == 8) { // ルール8を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule8(rep.get(i) - PartList.size() - 1, bass_judge);
                                    System.out.println("変換後");
                                    System.out.println(new_SEQList);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);
                              } else if (rep.get(i + 1) == 9) { // ルール9を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule9(rep.get(i) - PartList.size() - 1, bass_judge);
                                    System.out.println("返還後は");
                                    System.out.println(new_SEQList);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);
                              } else if (rep.get(i + 1) == 10) { // ルール10を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule10(rep.get(i) - PartList.size() - 1, bass_judge);
                                    System.out.println("返還後は");
                                    System.out.println(new_SEQList);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);
                              } else if (rep.get(i + 1) == 11) { // ルール11を適応
                                    j++;
                                    new_SEQList = MyChromosome.rule11(rep.get(i) - PartList.size() - 1, bass_judge);
                                    System.out.println("返還後は");
                                    System.out.println(new_SEQList);
                                    new_SEQList = MyChromosome.priority(new_SEQList, rep.get(i) - 1, judge);
                                    Part_generation(new_SEQList, j, newScc);
                              }

                              if (judge == 1) {
                                    judge++;

                              }
                        } else {
                              j++;
                              Part_generation(new_SEQList, j, newScc);

                        }

                  }

                  if (j > 0) {
                        newScc.toMIDIXML().writefileAsSMF("output.mid");

                  }

                  // 新しいmidファイルを作る

                  System.out.println("");

            } catch (Exception e) {
                  e.printStackTrace();
            }

      }

      static void Part_generation(LinkedList<MutableMusicEvent> new_SEQList, int j, SCCDataSet newScc) {
            LinkedList<LinkedList<MutableMusicEvent>> new_NSList = new LinkedList(); // 新しい小節生成
            LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> new_sectionList = new LinkedList();// 新しいパートのリスト
            SCCDataSet.Part newPart0;

            // 小節にぶち込む
            new_NSList.add(new_SEQList);
            // パートにぶち込む
            new_sectionList.add(new_NSList);
            // midiファイルにするための新しいパート生成
            newPart0 = newScc.addPart(j, j);
            WritePart.WritePart(new_sectionList, newPart0);

      }

      // ベース決め
      static int in_bass(SCCDataSet.Part[] partlist) {

            LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> sectionList = new LinkedList();
            LinkedList<LinkedList<MutableMusicEvent>> NSList = new LinkedList(); // 新しい小節生成
            LinkedList<MutableMusicEvent> SEQList = new LinkedList(); // 新しい和音リスト生成

            int bass_part = 1;
            float low = 1000;
            for (int i = 0; i < partlist.length; i++) {

                  MutableMusicEvent[] notelist = partlist[i].getNoteOnlyList(); // 順にパートの音符を取得
                  float ave_notenum = 0;
                  float count = 0;

                  if (i != 0 && i != 9) {
                        for (int j = 0; j < notelist.length; j++) { // noteの中身
                              ave_notenum += notelist[j].notenum();
                              count++;
                        }
                        ave_notenum /= count;
                        System.out.println("");
                        System.out.println(i + "番目の平均の音の高さは" + ave_notenum);
                        if (low > ave_notenum) {
                              bass_part = i;
                              low = ave_notenum;
                        }
                  }

            }
            System.out.println("この曲のベースパートは " + bass_part);
            // MutableMusicEvent[] notelist = partlist[bass_part - 1].getNoteOnlyList(); //
            // 0番目のパートの音符を取得

            return bass_part;
      }

      // 矩形波のパート作り
      static LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> generate_sectionList(MutableMusicEvent[] notelist) {

            LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> sectionList = new LinkedList();
            LinkedList<LinkedList<MutableMusicEvent>> NSList = new LinkedList(); // 新しい小節生成
            LinkedList<MutableMusicEvent> SEQList = new LinkedList(); // 新しい和音リスト生成
            int a = 1;

            // for (MutableMusicEvent note : notelist) { // noteの中身
            // System.out.println(note);
            // }

            while (notelist[0].onset() >= 1920 * a) {
                  NSList = new LinkedList(); // 新しい小節生成
                  SEQList = new LinkedList(); // 新しい和音リスト生成

                  NSList.add(SEQList); // 小節リストに和音リスト追加
                  sectionList.add(NSList); // パートに小節リストを追加
                  a++;
            }

            NSList = new LinkedList(); // 新しい小節生成
            SEQList = new LinkedList(); // 新しい和音リスト生成

            SEQList.add(notelist[0]); // 和音リストに音符を追加
            NSList.add(SEQList); // 小節リストに和音リスト追加
            sectionList.add(NSList); // パートに小節リストを追加

            for (int i = 1; i < notelist.length; i++) { // noteの中身

                  int mesures = (int) (Math.ceil(notelist[i].onset() / (480 * 4)));// 小節数

                  int before_mesure = (int) (Math.ceil(notelist[i - 1].onset() / (480 * 4)));

                  int before_difference = (int) notelist[i].onset() - (int) notelist[i - 1].offset();

                  while (before_difference >= 1920) {
                        NSList = new LinkedList(); // 新しい小節生成
                        SEQList = new LinkedList(); // 新しい和音リスト生成
                        NSList.add(SEQList); // 小節リストに和音リスト追加
                        sectionList.add(NSList); // パートに小節リストを追加
                        before_difference -= 1920;
                  }

                  if ((before_mesure == mesures) && (mesures + 1) * 1920 >= notelist[i].offset() / (480 * 4)) {
                        if (notelist[i].onset() != notelist[i - 1].onset()) {

                              SEQList = new LinkedList(); // 新しい和音リスト生成
                              SEQList.add(notelist[i]); // 新しい和音リストの最後に音符を追加
                              sectionList.getLast().add(SEQList); // 和音リストをパートに追加
                        } else { // 同時になってたら（和音だったら）
                              sectionList.getLast().getLast().add(notelist[i]); // 和音リストの最後に音符を追加
                        }
                  } else if (i == 1 || ((notelist[i].offset() > sectionList.size() * 480 * 4))
                              || mesures != (int) (Math.ceil(notelist[i - 1].offset() / (480 * 4)))) {

                        NSList = new LinkedList(); // 新しい小節生成
                        SEQList = new LinkedList(); // 新しい和音リスト生成
                        SEQList.add(notelist[i]); // 和音リストに音符を追加
                        NSList.add(SEQList); // 小節リストに和音リスト追加
                        sectionList.add(NSList); // パートに小節リストを追加
                        // i番目の音符が小節内に収まっていたら
                  } else if (notelist[i].offset() <= sectionList.size() * 480 * 4) {
                        if (notelist[i].onset() != notelist[i - 1].onset()) {
                              SEQList = new LinkedList(); // 新しい和音リスト生成
                              SEQList.add(notelist[i]); // 新しい和音リストの最後に音符を追加
                              sectionList.getLast().add(SEQList); // 和音リストをパートに追加
                        } else { // 同時になってたら（和音だったら）
                              sectionList.getLast().getLast().add(notelist[i]); // 和音リストの最後に音符を追加
                        }
                  }

            }

            return sectionList;

      }

      // ドラムパートの分解
      static void generate_Drum_sectionList(MutableMusicEvent[] notelist, List<Integer> Drum_num, int t_or_n) {
            LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> sectionList = new LinkedList();
            LinkedList<LinkedList<MutableMusicEvent>> NSList = new LinkedList(); // 新しい小節生成
            LinkedList<MutableMusicEvent> SEQList = new LinkedList(); // 新しい和音リスト生成
            int a = 1;
            int count = 0;
            int b = 0;
            int c = 1;
            int first_i = 0;

            // 何番目がそのドラム音の最初の音か調べる
            for (int i = 0; i < notelist.length; i++) { // noteの中身

                  if (Drum_num.contains((int) notelist[i].notenum())) {
                        first_i = i;

                        break;
                  }

            }

            // 三角波かノイズか区別する

            while (notelist[first_i].onset() >= 1920 * a) {
                  NSList = new LinkedList(); // 新しい小節生成
                  SEQList = new LinkedList(); // 新しい和音リスト生成

                  NSList.add(SEQList); // 小節リストに和音リスト追加
                  sectionList.add(NSList); // パートに小節リストを追加
                  a++;
            }

            NSList = new LinkedList(); // 新しい小節生成
            SEQList = new LinkedList(); // 新しい和音リスト生成
            if (Drum_num.contains((int) notelist[first_i].notenum())) {

                  SEQList.add(new MutableNote(notelist[first_i].onset(), notelist[first_i].onset() + 120, 60,
                              notelist[first_i].velocity(), 240)); // 和音リストに音符を追加
                  count++;
            }
            NSList.add(SEQList); // 小節リストに和音リスト追加
            sectionList.add(NSList); // パートに小節リストを追加
            System.out.println("楽譜作る");
            c = first_i;
            for (int i = 1; i < notelist.length; i++) { // noteの中身

                  int mesures = (int) (Math.ceil(notelist[i].onset() / (480 * 4)));// 小節数

                  int before_mesure = (int) (Math.ceil(notelist[c].onset() / (480 * 4)));

                  int before_difference = (int) notelist[i].onset() - (int) notelist[c].offset();

                  if (Drum_num.contains((int) notelist[i].notenum())) {
                        while (before_difference >= 1920) {

                              NSList = new LinkedList(); // 新しい小節生成
                              SEQList = new LinkedList(); // 新しい和音リスト生成
                              NSList.add(SEQList); // 小節リストに和音リスト追加
                              sectionList.add(NSList); // パートに小節リストを追加
                              before_difference -= 1920;
                        }

                        b++;

                        // 小節以内だったら

                        if ((before_mesure == mesures) && (mesures + 1) * 1920 >= notelist[c].offset() / (480 * 4)) {

                              count++;
                              if (notelist[i].onset() != notelist[c].onset()) {

                                    SEQList = new LinkedList(); // 新しい和音リスト生成
                                    SEQList.add(new MutableNote(notelist[i].onset(), notelist[i].onset() + 120, 60,
                                                notelist[i].velocity(), 240)); // 新しい和音リストの最後に音符を追加
                                    sectionList.getLast().add(SEQList); // 和音リストをパートに追加
                              }
                        } else if (b == 1 || ((notelist[i].offset() > sectionList.size() * 480 * 4))
                                    || mesures != (int) (Math.ceil(notelist[c].offset() / (480 * 4)))) {
                              System.out.println();

                              NSList = new LinkedList(); // 新しい小節生成
                              SEQList = new LinkedList(); // 新しい和音リスト生成
                              SEQList.add(new MutableNote(notelist[i].onset(), notelist[i].onset() + 120, 60,
                                          notelist[i].velocity(), 240)); // 和音リストに音符を追加
                              NSList.add(SEQList); // 小節リストに和音リスト追加
                              sectionList.add(NSList); // パートに小節リストを追加
                              c = i;
                              // i番目の音符が小節内に収まっていたら
                        } else if (notelist[i].offset() <= sectionList.size() * 480 * 4) {

                              if (notelist[i].onset() != notelist[c].onset()) {
                                    SEQList = new LinkedList(); // 新しい和音リスト生成
                                    SEQList.add(new MutableNote(notelist[i].onset(), notelist[i].onset() + 120, 60,
                                                notelist[i].velocity(), 240)); // 新しい和音リストの最後に音符を追加
                                    sectionList.getLast().add(SEQList); // 和音リストをパートに追加
                              }
                        }
                        // System.out.println(sectionList);
                        // System.out.println(notelist[i]);

                  }

            }

            if (count != 0) {
                  if (t_or_n == 1) {
                        tri_list.add(sectionList);
                  } else if (t_or_n == 2) {
                        noise_list.add(sectionList);
                  }
                  Drum_PartList.add(sectionList);

            }

            // ---------------------------

      }

      static LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> add_sectionList(MutableMusicEvent[] notelist,
                  LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> sectionList) {
            return sectionList;

      }

      static int Mesure_Calculation(SCCDataSet.Part[] partlist) {
            int mesure = 0;
            float last_OffSet = 0;
            float Max_last_OffSet = 0;

            for (int i = 0; i < partlist.length; i++) {

                  MutableMusicEvent[] notelist = partlist[i].getNoteOnlyList(); // 順にパートの音符を取得
                  for (MutableMusicEvent note : notelist) { // noteの中身
                        last_OffSet = note.offset();
                  }
                  if (Max_last_OffSet < last_OffSet) {
                        Max_last_OffSet = (int) last_OffSet;
                  }
            }
            mesure = (int) (Math.ceil(Max_last_OffSet / (480 * 4)));

            return mesure;
      }

      static int emptiness(int num) {

            if (num <= PartList.size() - 1) {

                  if (PartList.get(num).get(0).get(0).size() == 0) {
                        return 1;
                  }

            } else if (num - square_list.size() <= Drum_PartList.size()) {

                  if (Drum_PartList.get(num - square_list.size() - 1).get(0).get(0).size() == 0) {
                        return 1;
                  }
            }
            return 0;
      }

}