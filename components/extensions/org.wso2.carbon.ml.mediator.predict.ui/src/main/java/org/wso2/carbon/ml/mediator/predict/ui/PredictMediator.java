/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ml.mediator.predict.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapsePath;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

import java.util.*;

import static org.wso2.carbon.ml.mediator.predict.ui.PredictMediatorConstants.*;


public class PredictMediator extends AbstractMediator {

    private String modelStorageLocation;
    private String predictionPropertyName;
    private List<MediatorProperty> features = new ArrayList<MediatorProperty>();
    private String percentile;

    @Override
    public OMElement serialize(OMElement parent) {

        OMElement mlElement = fac.createOMElement(PREDICT_QNAME);
        saveTracingState(mlElement, this);

        if (modelStorageLocation != null) {
            OMElement modelElement = fac.createOMElement(MODEL_QNAME);
            modelElement.addAttribute(
                    fac.createOMAttribute(STORAGE_LOCATION_ATT.getLocalPart(), nullNS, modelStorageLocation));
            mlElement.addChild(modelElement);
        } else {
            throw new MediatorException("Invalid Predict mediator. Model storage-location is required");
        }

        if (percentile != null) {
            OMElement percentileElement = fac.createOMElement(PERCENTILE_QNAME);
            percentileElement.addAttribute(fac.createOMAttribute(VALUE_ATT.getLocalPart(), nullNS, percentile));
            mlElement.addChild(percentileElement);
        }

        if (features.isEmpty()) {
            throw new MediatorException("Invalid Predict mediator. Features required");
        }
        OMElement featuresElement = fac.createOMElement(FEATURES_QNAME);
        for (MediatorProperty mediatorProperty : features) {
            OMElement featureElement = fac.createOMElement(FEATURE_QNAME);
            if (mediatorProperty.getName() == null) {
                throw new MediatorException("Invalid Predict mediator. Feature names are required.");
            }
            featureElement
                    .addAttribute(fac.createOMAttribute(NAME_ATT.getLocalPart(), nullNS, mediatorProperty.getName()));
            if (mediatorProperty.getExpression() == null) {
                throw new MediatorException("Invalid Predict mediator. Feature expressions are required.");
            }
            SynapseXPathSerializer.serializeXPath(mediatorProperty.getExpression(), featureElement,
                    EXPRESSION_ATT.getLocalPart());
            featuresElement.addChild(featureElement);
        }
        mlElement.addChild(featuresElement);

        if (predictionPropertyName != null) {
            OMElement predictionElement = fac.createOMElement(PREDICTION_OUTPUT_QNAME);
            predictionElement
                    .addAttribute(fac.createOMAttribute(PROPERTY_ATT.getLocalPart(), nullNS, predictionPropertyName));
            mlElement.addChild(predictionElement);
        } else {
            throw new MediatorException("Invalid Predict mediator. PredictionOutput property name is required");
        }

        if (parent != null) {
            parent.addChild(mlElement);
        }
        return mlElement;
    }

    @Override
    public void build(OMElement omElement) {

        // model
        OMElement modelElement = omElement.getFirstChildWithName(MODEL_QNAME);
        if (modelElement == null) {
            throw new MediatorException("Model element is required.");
        }
        OMAttribute modelName = modelElement.getAttribute(STORAGE_LOCATION_ATT);
        if(modelName == null) {
            throw new MediatorException("Model storage-location attribute is required.");
        }
        this.modelStorageLocation = modelName.getAttributeValue();

        // percentile
        OMElement percentileElement = omElement.getFirstChildWithName(PERCENTILE_QNAME);
        if(percentileElement != null) {
            OMAttribute percentileValue = percentileElement.getAttribute(VALUE_ATT);
            if (percentileValue == null) {
                this.percentile = "95.0";
            }
            this.percentile = percentileValue.getAttributeValue();
        }

        // features
        OMElement featuresElement = omElement.getFirstChildWithName(FEATURES_QNAME);
        if(featuresElement == null) {
            throw new MediatorException("Features element is required.");
        }

        // feature
        for (Iterator it = featuresElement.getChildrenWithName(FEATURE_QNAME); it.hasNext();) {
            OMElement featureElement = (OMElement) it.next();
            OMAttribute featureName = featureElement.getAttribute(NAME_ATT);
            if(featureName == null || featureName.getAttributeValue() == null
                    || "".equals(featureName.getAttributeValue())) {
                throw new MediatorException("Feature name is required.");
            }

            OMAttribute expression = featureElement.getAttribute(EXPRESSION_ATT);
            if (expression != null && expression.getAttributeValue() != null) {
                MediatorProperty mediatorProperty = new MediatorProperty();
                mediatorProperty.setName(featureName.getAttributeValue());
                try {
                    SynapseXPath synapsePath = SynapseXPathFactory.getSynapseXPath(featureElement, EXPRESSION_ATT);
                    mediatorProperty.setExpression(synapsePath);
                    this.addFeature(mediatorProperty);
                    // TODO support json path
                } catch (JaxenException e) {
                    throw new MediatorException("Error while extracting expression");
                }

            } else {
                throw new MediatorException("feature expression is required.");
            }

        }

        // prediction
        OMElement predictionElement = omElement.getFirstChildWithName(PREDICTION_OUTPUT_QNAME);
        if (predictionElement == null) {
            throw new MediatorException("Prediction element is required.");
        }
        OMAttribute predictionExpression = predictionElement.getAttribute(PROPERTY_ATT);
        if(predictionExpression == null) {
            throw new MediatorException("Prediction property attribute is required.");
        }
        this.predictionPropertyName = predictionExpression.getAttributeValue();

        processAuditStatus(this, omElement);
    }

    @Override
    public String getTagLocalName() {
        return PREDICT_TAG_LOCAL_NAME;
    }

    /**
     * Get model storage location
     * @return the path to MLModel file
     */
    public String getModelStorageLocation() {
        return modelStorageLocation;
    }

    /**
     * Set model storage location
     * @param modelStorageLocation path to MLModel file
     */
    public void setModelStorageLocation(String modelStorageLocation) {
        this.modelStorageLocation = modelStorageLocation;
    }

    /**
     * Get the property name to which the prediction value is set
     * @return
     */
    public String getPredictionPropertyName() {
        return predictionPropertyName;
    }

    /**
     * Set the prediction property name
     * @param predictionProperty message context property name
     */
    public void setPredictionPropertyName(String predictionProperty) {
        this.predictionPropertyName = predictionProperty;
    }

    /**
     * Add a feature
     * @param mediatorProperty MediatorProperty containing the feature name, expression
     */
    public void addFeature(MediatorProperty mediatorProperty) {
        this.features.add(mediatorProperty);
    }

    /**
     * Get mapping xpath or json path expression for the feature name
     * @param featureName name of the feature
     * @return the xpath or json path expression
     */
    public SynapsePath getExpressionForFeature(String featureName) {
        for(MediatorProperty mediatorProperty : features) {
            if(mediatorProperty.getName().equals(featureName)) {
                return mediatorProperty.getExpression();
            }
        }
        return null;
    }

    /**
     * Get the list of feature mappings
     * @return MediatorProperty list
     */
    public List<MediatorProperty> getFeatures() {
        return features;
    }

    /**
     * Get the percentile value for anomaly detection
     * @return percentile
     */
    public String getPercentile() {
        return percentile;
    }

    /**
     * Set the percentile value
     * @param percentile percentile value for anomaly detection
     */
    public void setPercentile(String percentile) {
        this.percentile = percentile;
    }
}
