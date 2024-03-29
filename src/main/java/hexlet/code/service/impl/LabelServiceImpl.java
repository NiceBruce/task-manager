package hexlet.code.service.impl;

import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.service.LabelService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;

    @Override
    public Label createLabel(LabelDto dto) {
        final Label label = new Label();
        label.setName(dto.getName());
        return labelRepository.save(label);
    }

    @Override
    public Label updateLabel(long id, LabelDto dto) {
        Label labelToUpdate = getCurrentLabelById(id);
        labelToUpdate.setName(dto.getName());
        return labelRepository.save(labelToUpdate);
    }

    @Override
    public Label getCurrentLabelById(long id) {

        return labelRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }
}
