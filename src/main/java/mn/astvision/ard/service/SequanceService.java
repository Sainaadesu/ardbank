package mn.astvision.ard.service;

import org.springframework.stereotype.Service;
import mn.astvision.ard.data.Sequence;

@Service
public class SequanceService {
    Sequence sequence;
    public void sequenceInc(){
        String data = (String) sequence.getSequence_data();
        Long firstNum = Long.valueOf(data);
        sequence.setSequence_data(String.valueOf(firstNum + 1));
    }
}
