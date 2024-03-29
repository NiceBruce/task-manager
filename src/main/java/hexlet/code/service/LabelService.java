package hexlet.code.service;

import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;

public interface LabelService {
    Label createLabel(LabelDto dto);

    Label updateLabel(long id, LabelDto dto);

    Label getCurrentLabelById(long id);
}
