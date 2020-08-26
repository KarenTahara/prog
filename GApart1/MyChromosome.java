import org.apache.commons.math3.genetics.*;
import java.util.*;

import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.processing.CMXController;
import jp.crestmuse.cmx.elements.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

class MyChromosome extends BinaryChromosome {
      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> PartList;
      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> Drum_PartList;

      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> square_list;
      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> tri_list;
      static LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> noise_list;

      double[] test = new double[20];// 初期確率
      static double gof;// goodness_of_fit返す適合度
      static int first, after;
      static int part_count;
      static int judge = 0;
      static int per8 = 240; // 8分音符１こあたりの長さ
      static LinkedList<MutableMusicEvent> l = new LinkedList<MutableMusicEvent>();

      // オンセット情報を入れておく
      static List<Integer> square_onset;
      static List<Integer> tri_onset = new LinkedList();
      static List<Integer> noise_onset = new LinkedList();

      // 三角波の優先順位の高い音情報を入れておく
      static LinkedList<MutableMusicEvent> tri_note;
      static LinkedList<MutableMusicEvent> noise_note;

      static float notenum_ave;

      // req = パートと変換ルールをランダムに2個生成したもの
      // 初期個体をランダムに個体を１００個作る
      MyChromosome(List<Integer> rep, LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> PartList1,
                  LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> Drum_PartList1, int part_count1,
                  LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> square_list1,
                  LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> tri_list1,
                  LinkedList<LinkedList<LinkedList<LinkedList<MutableMusicEvent>>>> noise_list1) {
            // 親クラスのコンストラクタを呼ぶ
            super(rep);

            PartList = PartList1;
            part_count = part_count1;
            Drum_PartList = Drum_PartList1;
            square_list = square_list1;
            tri_list = tri_list1;
            noise_list = noise_list1;

            for (int i = 0; i < rep.size(); i++) {
                  test[i] = (double) rep.get(i);
            }

      }

      protected void checkValidity(List<Integer> rep) {
            // do nothing
      }

      public MyChromosome newFixedLengthChromosome(List<Integer> rep) {

            return new MyChromosome(rep, PartList, Drum_PartList, part_count, square_list, tri_list, noise_list);
      }

      public List<Integer> getRepresentation() {
            return super.getRepresentation(); // complete rep
      }

      public double fitness() {// 適合度
            System.out.println("");
            System.out.println("");

            double Goodness1 = 0, Goodness2 = 0, Goodness3 = 0, Goodness4 = 0, Goodness5 = 0, Goodness6 = 0,
                        Goodness7 = 0;
            double Goodness8 = 0, Goodness9 = 0, Goodness10 = 0, Goodness11 = 0;

            List<Integer> rep = getRepresentation();

            double count = 0;
            System.out.println();
            Set<Double> checkHash = new HashSet<Double>();
            System.out.println("rep=" + rep);

            LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> sectionList = new LinkedList();// パートリスト

            judge = 0;

            Boolean result = false;

            for (int i = 0; i < rep.size(); i += 2) {
                  if (checkHash.contains(test[i])) {
                        // 重複があればtrueをセットし終了
                        result = true;
                        break;
                  } else {
                        // 重複しなければハッシュセットへ追加
                        checkHash.add(test[i]);
                  }
            }
            if (result == true) {
                  System.out.println("パート番号が重なっている ");
                  return -1000000;
            } else {
                  LinkedList<MutableMusicEvent> NSList;
                  tri_note = new LinkedList<MutableMusicEvent>();
                  noise_note = new LinkedList<MutableMusicEvent>();
                  int bass_judge;

                  for (int i = 0; i < rep.size(); i += 2) {
                        System.out.println("i= " + i);
                        if (test[i] == 1) {
                              bass_judge = 1;

                        } else {
                              bass_judge = 0;
                        }

                        // もしパート番号だったら（0,2,4,6...番目）
                        // ドラムパート以外の処理
                        System.out.println("bass_judge  :" + bass_judge);
                        if (test[i] <= PartList.size() && test[i] != 1) {

                              if (PartList.get(rep.get(i) - 1).get(0).get(0).size() != 0) {

                                    // ノートナンバー の平均値を出す
                                    notenum_ave = ave(rep.get(i) - 1);

                                    if (test[i + 1] == 1) {// もし次の要素が適応ルール１だったら
                                          System.out.println("パート" + test[i] + "にルール1を適応する");
                                          NSList = rule1(rep.get(i) - 1);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness1 += rule1_gof(test[i]);

                                    } else if (test[i + 1] == 2) {
                                          System.out.println("パート" + test[i] + "にルール2を適応する");
                                          NSList = rule2(rep.get(i) - 1);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness2 += rule2_gof(test[i]);

                                    } else if (test[i + 1] == 3) {
                                          System.out.println("パート" + test[i] + "にルール3を適応する");
                                          NSList = rule3(rep.get(i) - 1);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness3 += rule3_gof(test[i]);

                                    } else if (test[i + 1] == 4) {
                                          System.out.println("パート" + test[i] + "にルール4を適応する");
                                          NSList = rule4(rep.get(i) - 1);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness4 += rule4_gof(test[i]);

                                    } else if (test[i + 1] == 5) {
                                          System.out.println("パート" + test[i] + "にルール5を適応する");
                                          NSList = rule5(rep.get(i) - 1);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness5 += rule5_gof(test[i]);

                                    } else if (test[i + 1] == 6) {
                                          System.out.println("パート" + test[i] + "にルール6を適応する");
                                          NSList = rule6(rep.get(i) - 1);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness6 += rule6_gof(test[i]);

                                    } else if (test[i + 1] == 7) {
                                          System.out.println("パート" + test[i] + "にルール7を適応する");
                                          NSList = rule7(rep.get(i) - 1);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness7 += rule7_gof(test[i]);

                                    } else {
                                          Goodness11 = -1000000;
                                    }
                              }
                              // ドラムパートだった時の処理

                        } else if ((test[i] == 1 && test[i + 1] > 7) || test[i] <= part_count) {
                              System.out.println("dsads");
                              if (test[i] == 1 || Drum_PartList.get(rep.get(i) - PartList.size() - 1).get(0).get(0)
                                          .size() != 0) {
                                    System.out.println("パ");
                                    if (test[i + 1] == 8) {
                                          System.out.println("パート" + test[i] + "にルール8を適応する");
                                          NSList = rule8(rep.get(i) - PartList.size() - 1, bass_judge);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness8 += rule8_gof();

                                    } else if (test[i + 1] == 9) {
                                          System.out.println("パート" + test[i] + "にルール9を適応する");
                                          NSList = rule9(rep.get(i) - PartList.size() - 1, bass_judge);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness9 += rule9_gof();

                                    } else if (test[i + 1] == 10) {
                                          System.out.println("パート" + test[i] + "にルール10を適応する");
                                          NSList = rule10(rep.get(i) - PartList.size() - 1, bass_judge);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness10 += rule10_gof();

                                    } else if (test[i + 1] == 11) {
                                          System.out.println("パート" + test[i] + "にルール11を適応する");
                                          NSList = rule11(rep.get(i) - PartList.size() - 1, bass_judge);
                                          NSList = priority(NSList, rep.get(i) - 1, judge);
                                          Goodness11 += rule11_gof();

                                    } else {
                                          Goodness11 = -1000000;
                                    }
                              } else {

                              }

                        } else if (test[i] > part_count || test[i] > PartList.size()) {

                              Goodness11 = -1000000;
                        }
                        double Goodnesss = Goodness1 + Goodness2 + Goodness3 + Goodness4 + Goodness5 + Goodness6
                                    + Goodness7 + Goodness8 + Goodness9 + Goodness10 + Goodness11;
                        System.out.println("適性度i=　" + i + "====" + Goodnesss);
                  }
                  double Goodness = Goodness1 + Goodness2 + Goodness3 + Goodness4 + Goodness5 + Goodness6 + Goodness7
                              + Goodness8 + Goodness9 + Goodness10 + Goodness11;
                  System.out.println("適性度　" + Goodness);
                  tri_note = new LinkedList<MutableMusicEvent>();
                  noise_note = new LinkedList<MutableMusicEvent>();
                  return Goodness;
            }

      }

      // 優先順位ごとに音を削除していく
      public static LinkedList<MutableMusicEvent> priority(LinkedList<MutableMusicEvent> l, int num, int judge) {
            after = 0;
            if (judge == 1) {
                  square_onset = new LinkedList();
                  tri_onset = new LinkedList();
            }

            LinkedList<MutableMusicEvent> s = new LinkedList<MutableMusicEvent>();

            // 矩形波の時
            if (num != 0 && PartList.size() > num) {
                  // System.out.println("このパートは矩形波です、音符をそのまま返します");
                  return l;
                  // 三角波だったら
            } else if (num < PartList.size() + tri_list.size()) {
                  // System.out.println("この音は三角波です、削除前は");

                  // System.out.println(l);
                  for (int i = 0; i < l.size(); i++) {
                        // 音が重なっていたらカウントしない
                        int flag = 0;

                        for (int j = 0; j < tri_note.size(); j++) {
                              // 音が重なっていたら
                              if ((int) tri_note.get(j).onset() == (int) l.get(i).onset()) {
                                    flag = 1;
                              }
                        }
                        if (flag == 0) {
                              s.add(l.get(i));
                        }
                  }
                  for (int i = 0; i < l.size(); i++) {
                        tri_note.add(l.get(i));
                  }

                  System.out.println("削除した音は");
                  // System.out.println(s);

                  // ノイズだったら
            } else {
                  System.out.println("この音はノイズです　削除前は");
                  // System.out.println(l);
                  for (int i = 0; i < l.size(); i++) {
                        // 音が重なっていたらカウントしない
                        int flag = 0;

                        for (int j = 0; j < noise_note.size(); j++) {
                              // 音が重なっていたら

                              if ((int) noise_note.get(j).onset() == (int) l.get(i).onset()) {
                                    flag = 1;
                              }
                        }
                        if (flag == 0) {
                              s.add(l.get(i));
                        }
                  }
                  for (int i = 0; i < l.size(); i++) {
                        noise_note.add(l.get(i));
                  }

            }
            l = new LinkedList(); // 新しい和音リスト生成;
            l = s;
            return l;

      }

      // 【変換ルール１】上の音だけを残す適合度
      public static LinkedList<MutableMusicEvent> rule1(int part_num) {
            first = 0;
            after = 0;
            l = new LinkedList<MutableMusicEvent>();
            System.out.println("【変換ルール１】");
            for (int n = 0; n < PartList.get(part_num).size(); n++) {
                  if (PartList.get(part_num).get(n).get(0).size() != 0) {
                        for (int i = 0; i < PartList.get(part_num).get(n).size(); i++) {

                              int max_notenum = PartList.get(part_num).get(n).get(i).get(0).notenum();
                              int max_i = 0;

                              for (int j = 0; j < PartList.get(part_num).get(n).get(i).size(); j++) {
                                    first++;
                                    if (PartList.get(part_num).get(n).get(i).get(j).notenum() > max_notenum) {
                                          max_i = j;
                                          max_notenum = PartList.get(part_num).get(n).get(i).get(j).notenum();
                                    }
                              }
                              l.add(PartList.get(part_num).get(n).get(i).get(max_i));
                              after++;
                        }
                  }
            }

            return l;
      }

      // 【変換ルール２】下の音だけを残す
      public static LinkedList<MutableMusicEvent> rule2(int part_num) {
            first = 0;
            after = 0;
            l = new LinkedList<MutableMusicEvent>();
            System.out.println("【変換ルール2】");
            for (int n = 0; n < PartList.get(part_num).size(); n++) {
                  // System.out.println(n + "パート目 ：サイズ＝" +
                  // PartList.get(part_num).get(n).get(0).size() + "出力 = "
                  // + PartList.get(part_num).get(n));
                  if (PartList.get(part_num).get(n).get(0).size() != 0) {
                        // System.out.println("からじゃない");

                        for (int i = 0; i < PartList.get(part_num).get(n).size(); i++) {

                              int min_notenum = PartList.get(part_num).get(n).get(i).get(0).notenum();
                              int min_i = 0;

                              for (int j = 0; j < PartList.get(part_num).get(n).get(i).size(); j++) {
                                    first++;
                                    if (PartList.get(part_num).get(n).get(i).get(j).notenum() < min_notenum) {
                                          min_i = j;
                                          min_notenum = PartList.get(part_num).get(n).get(i).get(j).notenum();
                                    }
                              }
                              l.add(PartList.get(part_num).get(n).get(i).get(min_i));
                              after++;
                        }
                  }
            }

            return l;
      }

      // 【変換ルール３】 3和音の8分音符分散和音
      public static LinkedList<MutableMusicEvent> rule3(int part_num) {
            first = 0;
            after = 8;

            l = new LinkedList<MutableMusicEvent>();

            int per8 = 240; // 8分音符１こあたりの長さ

            for (int k = 0; k < PartList.get(part_num).size(); k++) {
                  judge = 0;
                  int b = k;
                  if (PartList.get(part_num).get(k).get(0).size() != 0) {
                        if (PartList.get(part_num).get(k).get(0).size() >= 3) {// n番目の音が３つの和音だったら
                              // もし連続する３和音が同じ音ではなかったら適合度-大きな値を返す
                              Integer notenum[] = { PartList.get(part_num).get(k).get(0).get(0).notenum(),
                                          PartList.get(part_num).get(k).get(0).get(1).notenum(),
                                          PartList.get(part_num).get(k).get(0).get(2).notenum() };

                              for (int i = 1; i < PartList.get(part_num).get(k).size(); i++) {
                                    if (PartList.get(part_num).get(k).get(i).size() != 3) {
                                          judge = 1;
                                    } else {
                                          for (int j = 0; j < 3; j++) {
                                                if (Arrays.asList(notenum).contains((int) PartList.get(part_num).get(k)
                                                            .get(i).get(j).notenum())) {

                                                      judge = 1;
                                                }
                                          }
                                    }
                              }

                              Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7).stream().forEach(n -> {// asList()配列をリストに変換

                                    final int[] a = { b };
                                    int m = (n == 3 || n == 7) ? 1 : n % 4;// n=3ならm＝１、そうでなければm＝n
                                    long myOnset = PartList.get(part_num).get(a[0]).get(0).get(0).onset() + per8 * n;
                                    l.add(new MutableNote(myOnset, myOnset + per8,
                                                PartList.get(part_num).get(a[0]).get(0).get(m).notenum(),
                                                PartList.get(part_num).get(a[0]).get(0).get(m).velocity(), per8));
                              });
                        }

                        if (judge == 1) {
                              for (int i = 0; i < PartList.get(part_num).get(k).size(); i++) {
                                    l.add(PartList.get(part_num).get(k).get(i).get(0));
                              }
                        }
                  }
            }
            return l;

      }

      // 【変換ルール4】2和音の8分音符分散和音
      public static LinkedList<MutableMusicEvent> rule4(int part_num) {
            first = 0;
            after = 8;

            l = new LinkedList(); // 新しい和音リスト生成;

            for (int k = 0; k < PartList.get(part_num).size(); k++) {
                  System.out.println(k + "小節目の音＝＝＝＝＝＝＝＝＝＝＝" + PartList.get(part_num).get(k));
                  judge = 0;
                  int b = k;
                  if (PartList.get(part_num).get(k).get(0).size() != 0) {
                        if (PartList.get(part_num).get(k).get(0).size() >= 2) {// 0番目の音が2つの和音だったら

                              Integer notenum[] = { PartList.get(part_num).get(k).get(0).get(0).notenum(),
                                          PartList.get(part_num).get(k).get(0).get(1).notenum() };

                              for (int i = 1; i < PartList.get(part_num).get(k).size(); i++) {
                                    if (PartList.get(part_num).get(k).get(i).size() != 2) {
                                          judge = 1;
                                    } else {
                                          for (int j = 0; j < 2; j++) {
                                                if (Arrays.asList(notenum).contains((int) PartList.get(part_num).get(k)
                                                            .get(i).get(j).notenum())) {
                                                      System.out.println("入っちゃった");
                                                      judge = 1;
                                                }
                                          }
                                    }
                              }
                              if (judge == 0) {
                                    System.out.println("にわおん");
                                    // System.out.println("２和音です");
                                    Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7).stream().forEach(n -> {// asList()配列をリストに変換

                                          final int[] a = { b };
                                          int m = n % 2;
                                          long myOnset = PartList.get(part_num).get(a[0]).get(0).get(0).onset()
                                                      + per8 * n;
                                          l.add(new MutableNote(myOnset, myOnset + per8,
                                                      PartList.get(part_num).get(a[0]).get(0).get(m).notenum(),
                                                      PartList.get(part_num).get(a[0]).get(0).get(m).velocity(), per8));

                                    });
                              }

                        } else {

                              for (int i = 0; i < PartList.get(part_num).get(0).size(); i++) {
                                    l.add(PartList.get(part_num).get(0).get(i).get(0));
                              }
                        }
                  }
            }

            return l;

      }

      // 【変換ルール５】4和音の8分音符分散和音
      public static LinkedList<MutableMusicEvent> rule5(int part_num) {
            first = 0;
            after = 8;
            l = new LinkedList(); // 新しい和音リスト生成;

            for (int k = 0; k < PartList.get(part_num).size(); k++) {
                  int b = k;
                  judge = 0;
                  if (PartList.get(part_num).get(k).get(0).size() != 0) {
                        if (PartList.get(part_num).get(k).get(0).size() >= 4) {// 0番目の音が３つの和音だったら
                              // もし連続する３和音が同じ音ではなかったら適合度-大きな値を返す
                              Integer notenum[] = { PartList.get(part_num).get(k).get(0).get(0).notenum(),
                                          PartList.get(part_num).get(k).get(0).get(1).notenum(),
                                          PartList.get(part_num).get(k).get(0).get(2).notenum(),
                                          PartList.get(part_num).get(k).get(0).get(3).notenum() };

                              for (int i = 1; i < PartList.get(part_num).get(k).size(); i++) {
                                    if (PartList.get(part_num).get(k).get(i).size() != 4) {
                                          judge = 1;
                                    } else {
                                          for (int j = 0; j < 4; j++) {
                                                if (Arrays.asList(notenum).contains((int) PartList.get(part_num).get(k)
                                                            .get(i).get(j).notenum())) {
                                                      // System.out.println("配列内に値が存在する。");
                                                      judge = 1;
                                                }
                                          }
                                    }
                              }

                              Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7).stream().forEach(n -> {// asList()配列をリストに変換
                                    final int[] a = { b };
                                    int m = n % 4;// n=3ならm＝１、そうでなければm＝n
                                    long myOnset = PartList.get(part_num).get(a[0]).get(0).get(0).onset() + per8 * n;
                                    l.add(new MutableNote(myOnset, myOnset + per8,
                                                PartList.get(part_num).get(a[0]).get(0).get(m).notenum(),
                                                PartList.get(part_num).get(a[0]).get(0).get(m).velocity(), per8));
                              });

                        } else {
                              judge = 1;
                              for (int i = 0; i < PartList.get(part_num).get(k).size(); i++) {
                                    l.add(PartList.get(part_num).get(k).get(i).get(0));
                              }
                        }
                  }
            }
            return l;
      }

      // 【変換ルール６】上の音をのこし、かつ長さが４分音符以上のものがあったら8分音符へと変更
      public static LinkedList<MutableMusicEvent> rule6(int part_num) {
            l = new LinkedList(); // 新しい和音リスト生成;
            first = 0;
            after = 0;
            l = rule1(part_num);
            System.out.print("４部音符以上分解");
            l = eight_note(l);
            return l;
      }

      // 【変換ルール７】下の音をのこし、かつ長さが４分音符以上のものがあったら8分音符へと変更
      public static LinkedList<MutableMusicEvent> rule7(int part_num) {
            l = new LinkedList(); // 新しい和音リスト生成;
            first = 0;
            after = 0;
            l = rule2(part_num);
            System.out.print("４部音符以上分解");
            l = eight_note(l);
            return l;
      }

      // ８分音符にする
      public static LinkedList<MutableMusicEvent> eight_note(LinkedList<MutableMusicEvent> section) {
            LinkedList<MutableMusicEvent> new_section = new LinkedList(); // 新しい音リスト生成;
            for (int i = 0; i < section.size(); i++) {

                  int diff = (int) (section.get(i).offset() - section.get(i).onset());

                  if (diff >= 240) {
                        int j = 0;

                        while (diff >= 240) {

                              new_section.add(new MutableNote(section.get(i).onset() + 240 * j,
                                          section.get(i).onset() + 240 * j + 240, section.get(i).notenum(),
                                          section.get(i).velocity(), 240));
                              diff -= 240;
                              j++;
                        }

                  } else {
                        new_section.add(section.get(i));
                  }
            }

            return new_section;
      }

      // 【変換ルール８】 ８部音符レベルの裏拍を休符に置き換える

      public static LinkedList<MutableMusicEvent> rule8(int part_num, int b) {
            l = new LinkedList(); // 新しい音リスト生成;
            first = 0;

            if (part_num < tri_list.size()) {
                  System.out.println("三角波を変換します");
            } else {
                  System.out.println("ノイズを変換します");
            }

            System.out.println("元の音");
            if (b == 0) {
                  for (int k = 0; k < Drum_PartList.get(part_num).size(); k++) {

                        if (Drum_PartList.get(part_num).get(k).get(0).size() != 0) {
                              for (int i = 0; i < Drum_PartList.get(part_num).get(k).size(); i++) {

                                    if (Drum_PartList.get(part_num).get(k).get(i).get(0).onset() % 480 == 0) {
                                          l.add(Drum_PartList.get(part_num).get(k).get(i).get(0));

                                    }
                                    first++;
                              }
                        }
                  }
            } else {
                  for (int k = 0; k < PartList.get(0).size(); k++) {

                        if (PartList.get(0).get(k).get(0).size() != 0) {
                              for (int i = 0; i < PartList.get(0).get(k).size(); i++) {

                                    if (PartList.get(0).get(k).get(i).get(0).onset() % 480 == 0) {
                                          l.add(PartList.get(0).get(k).get(i).get(0));

                                    }
                                    first++;
                              }
                        }
                  }

            }
            System.out.println();
            return l;
      }

      // 【変換ルール9】 ８分音符レベルの表拍を休符に置き換える
      public static LinkedList<MutableMusicEvent> rule9(int part_num, int b) {
            l = new LinkedList(); // 新しい音リスト生成;
            first = 0;

            if (part_num < tri_list.size()) {
                  System.out.println("三角波を変換します");
            } else {
                  System.out.println("ノイズを変換します");
            }

            System.out.println("元の音");
            if (b == 0) {
                  for (int k = 0; k < Drum_PartList.get(part_num).size(); k++) {
                        if (Drum_PartList.get(part_num).get(k).get(0).size() != 0) {
                              for (int i = 0; i < Drum_PartList.get(part_num).get(k).size(); i++) {

                                    if (Drum_PartList.get(part_num).get(k).get(i).get(0).onset() % 480 != 0) {
                                          l.add(Drum_PartList.get(part_num).get(k).get(i).get(0));

                                    }
                                    first++;

                              }
                        }
                  }
            } else {
                  for (int k = 0; k < PartList.get(0).size(); k++) {
                        if (PartList.get(0).get(k).get(0).size() != 0) {
                              for (int i = 0; i < PartList.get(0).get(k).size(); i++) {

                                    if (PartList.get(0).get(k).get(i).get(0).onset() % 480 != 0) {
                                          l.add(PartList.get(0).get(k).get(i).get(0));

                                    }
                                    first++;
                              }
                        }
                  }
            }
            System.out.println();

            return l;
      }

      // 【変換ルール10】 4分音符レベルの裏拍を休符に置き換える
      public static LinkedList<MutableMusicEvent> rule10(int part_num, int b) {
            l = new LinkedList(); // 新しい音リスト生成;
            first = 0;

            if (part_num < tri_list.size()) {
                  System.out.println("三角波を変換します");
            } else {
                  System.out.println("ノイズを変換します");
            }
            System.out.println("元の音");
            if (b == 0) {
                  for (int k = 0; k < Drum_PartList.get(part_num).size(); k++) {
                        if (Drum_PartList.get(part_num).get(k).get(0).size() != 0) {
                              for (int i = 0; i < Drum_PartList.get(part_num).get(k).size(); i++) {

                                    if (Drum_PartList.get(part_num).get(k).get(i).get(0).onset() % 960 == 0) {
                                          l.add(Drum_PartList.get(part_num).get(k).get(i).get(0));

                                    }
                                    first++;
                              }
                        }
                  }
            } else {
                  for (int k = 0; k < PartList.get(0).size(); k++) {
                        if (PartList.get(0).get(k).get(0).size() != 0) {
                              for (int i = 0; i < PartList.get(0).get(k).size(); i++) {

                                    if (PartList.get(0).get(k).get(i).get(0).onset() % 960 == 0) {
                                          l.add(PartList.get(0).get(k).get(i).get(0));

                                    }
                                    first++;
                              }
                        }
                  }
            }
            System.out.println();
            return l;
      }

      // 【変換ルール11】 4分音符レベルのおもて拍を休符に置き換える
      public static LinkedList<MutableMusicEvent> rule11(int part_num, int b) {
            l = new LinkedList(); // 新しい音リスト生成;
            first = 0;

            if (part_num < tri_list.size()) {
                  System.out.println("三角波を変換します");
            } else {
                  System.out.println("ノイズを変換します");
            }
            System.out.println("元の音");
            if (b == 0) {
                  for (int k = 0; k < Drum_PartList.get(part_num).size(); k++) {
                        if (Drum_PartList.get(part_num).get(k).get(0).size() != 0) {
                              for (int i = 0; i < Drum_PartList.get(part_num).get(k).size(); i++) {

                                    if (Drum_PartList.get(part_num).get(k).get(i).get(0).onset() % 960 != 0) {

                                          l.add(Drum_PartList.get(part_num).get(k).get(i).get(0));

                                    }
                                    first++;
                              }
                        }
                  }
            } else {
                  for (int k = 0; k < PartList.get(0).size(); k++) {
                        if (PartList.get(0).get(k).get(0).size() != 0) {
                              for (int i = 0; i < PartList.get(0).get(k).size(); i++) {

                                    if (PartList.get(0).get(k).get(i).get(0).onset() % 960 != 0) {
                                          l.add(PartList.get(0).get(k).get(i).get(0));

                                    }
                                    first++;
                              }
                        }
                  }
            }
            System.out.println("");

            return l;
      }

      // 《変換ルール１》適合度計算
      public static double rule1_gof(double part) {
            if (part == 1) {
                  return 0;
            }

            return gof1(l) + gof2(first, after) + gof3(l);
      }

      // 《変換ルール2》適合度計算
      public static double rule2_gof(double part) {
            if (part == 1) {
                  return 0;
            }

            return gof1(l) + gof2(first, after) + gof3(l);
      }

      // 《変換ルール3》適合度計算
      public static double rule3_gof(double part) {

            if (part == 1 || l.size() == 0) {
                  return 0;
            }
            if (judge == 1) {
                  return -100000;
            } else {

                  return gof1(l) + gof2(first, 8) + gof3(l);
            }

      }

      // 《変換ルール4》適合度計算
      public static double rule4_gof(double part) {

            if (part == 1 || l == null) {
                  return 0;
            }
            if (judge == 1) {
                  return -100000;
            } else {

                  return gof1(l) + gof2(first, 8) + gof3(l);
            }

      }

      // 《変換ルール5》適合度計算
      public static double rule5_gof(double part) {

            if (part == 1 || l.size() == 0) {
                  return 0;
            }
            if (judge == 1) {
                  return -100000;
            } else {

                  return gof1(l) + gof2(first, 8) + gof3(l);
            }

      }

      // 《変換ルール6》適合度計算
      public static double rule6_gof(double part) {
            if (part == 1) {
                  return 0;
            }

            return gof1(l) + gof2(first, after) + gof3(l);
      }

      // 《変換ルール7》適合度計算
      public static double rule7_gof(double part) {
            if (part == 1) {
                  return 0;
            }

            return gof1(l) + gof2(first, after) + gof3(l);
      }

      // 《変換ルール8》適合度計算
      public static double rule8_gof() {
            return gof1(l);
      }

      // 《変換ルール9》適合度計算
      public static double rule9_gof() {
            return 1;
      }

      // 《変換ルール10》適合度計算
      public static double rule10_gof() {
            return 1;
      }

      // 《変換ルール11》適合度計算
      public static double rule11_gof() {
            return 1;
      }

      // 適合度１を計算（どれだけ音の動きがあるかどうか）^^^^^^^^^^^^^^^^^^^^^^^^^

      public static double gof1(LinkedList<MutableMusicEvent> section) {
            gof = 0;

            for (int i = 0; i < l.size() - 1; i++) {
                  // 音が重なっていたらカウントしない

                  if (section.get(i).offset() <= section.get(i + 1).onset()) {
                        gof += Math.abs(section.get(i).notenum() - section.get(i + 1).notenum());
                        gof++;
                  }
            }
            System.out.println("①　gof=" + gof);
            return gof;
      }

      // 適合度２を計算（元の音数より少なく、もしくは多くなっていないかどうか）
      public static double gof2(int first, int after) {
            System.out.println("②　gof=" + Math.abs(first - after));
            // return Math.abs(first - after);
            return 0;
      }

      // 適合度3を計算（平均値は元の音の平均ノートナンバーの平均値から離れていないか
      public static double gof3(LinkedList<MutableMusicEvent> section) {
            int afternotenum_ave = 0;
            float count = 0;
            for (int i = 0; i < section.size(); i++) {
                  afternotenum_ave += section.get(i).notenum();
                  count++;
            }
            afternotenum_ave /= count;
            System.out.println("③　gof=" + (Math.abs(notenum_ave - afternotenum_ave)));
            return 11 - Math.abs(notenum_ave - afternotenum_ave);

      }

      // ---------------------------------------------------------------------
      public static float ave(int num) {
            int first = 0;
            int count = 0;
            for (int i = 0; i < PartList.get(num).get(0).size(); i++) {
                  for (int j = 0; j < PartList.get(num).get(0).get(i).size(); j++) {
                        first += (int) PartList.get(num).get(0).get(i).get(j).notenum();
                        count++;
                  }
            }

            return (float) first / count;
      }
}
