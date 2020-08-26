import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.processing.CMXController;
import jp.crestmuse.cmx.elements.*;
import java.util.*;

public class WritePart {
    static void WritePart(LinkedList<LinkedList<LinkedList<MutableMusicEvent>>> newSectionList,
            SCCDataSet.Part newPart) {
        for (int i = 0; i < newSectionList.size(); i++) {
            for (int j = 0; j < newSectionList.get(i).size(); j++) {
                // 書き出し
                for (int k = 0; k < newSectionList.get(i).get(j).size(); k++) {
                    // パートに追加（onset offset notenum ベロシティ ベロシティ）
                    newPart.addNoteElement(newSectionList.get(i).get(j).get(k).onset(),
                            newSectionList.get(i).get(j).get(k).offset(), newSectionList.get(i).get(j).get(k).notenum(),
                            100, 100);
                }
            }

        }

    }
}