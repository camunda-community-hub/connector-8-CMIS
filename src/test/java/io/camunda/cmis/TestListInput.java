package io.camunda.cmis;

import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.cmis.CmisInput;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class TestListInput {

    @Test
    void testInput() {
        CmisInput input = new CmisInput();
        List<Map<String, Object>> listInputs = input.getInputParameters();
        assert (listInputs != null);
        // checkExistName(listInputs, CmisInput.FOLDER_NAME, null);


    }

    private void checkExistName(List<Map<String, Object>> listInputs, String name, String group) {
        Optional<Map<String, Object>> input = listInputs.stream().filter(t -> t.get("name").equals(name)).findFirst();
        assert (input.isPresent());
        if (group != null)
            assert (input.get().get(CherryInput.PARAMETER_MAP_GROUP_LABEL).equals(group));

    }
}
