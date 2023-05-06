/* ******************************************************************** */
/*   DeleteDocumentWorker                                                                   */
/*                                                                      */
/*  Delete a document, or a version in a document                                                   */
/*                                                                      */
/* ******************************************************************** */
package io.camunda.cherry.cmis.deletedocument;

import io.camunda.cherry.cmis.CmisCherryToolbox;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.cherry.definition.AbstractWorker;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

@Component
public class DeleteDocumentWorker extends AbstractWorker {

    public DeleteDocumentWorker() {
        super("c-cmis-deletedocument",
                Arrays.asList(
                    CmisCherryToolbox.CmisConnectionParameter
                ),
                Collections.emptyList(),
                Arrays.asList(CmisCherryToolbox.NO_CONNECTION_TO_CMIS));
    }

    @Override
    public String getName() {
        return "CMIS: Delete a document or a version";
    }
    @Override
    public String getDescription() {
        return "Delete a document in the CMIS repository, or a version in the document";
    }

    @Override
    public String getLogo() {
        return CmisCherryToolbox.getLogo();
    }

    @Override
    public String getCollectionName() {
        return CmisCherryToolbox.getCollectionName();
    }

    /**
     * @param jobClient        client
     * @param activatedJob     job activated
     * @param contextExecution context of this execution
     */
    @Override
    public void execute(final JobClient jobClient, final ActivatedJob activatedJob, AbstractWorker.ContextExecution contextExecution) {
    }


}
