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

package org.wso2.carbon.ml.commons.domain.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.util.List;

/**
 * DTO class for JAXB binding of machine-learner.xml
 */
@XmlRootElement(name = "MachineLearner")
@XmlAccessorType(XmlAccessType.FIELD)
public class MLConfiguration {

    @XmlElement(name = "Database")
    private String databaseName;

    @XmlElement(name = "HdfsURL")
    private String hdfsUrl;

    @XmlElement(name = "EmailNotificationEndpoint")
    private String emailNotificationEndpoint;

    @XmlElement(name = "ModelRegistryLocation")
    private String modelRegistryLocation;

    @XmlElementWrapper(name = "Properties")
    @XmlElement(name = "Property")
    private List<MLProperty> properties;

    @XmlElementWrapper(name = "Algorithms")
    @XmlElement(name = "Algorithm")
    private List<MLAlgorithm> mlAlgorithms;

    @XmlElement(name = "SummaryStatisticsSettings")
    private SummaryStatisticsSettings summaryStatisticsSettings;

    @XmlElement(name = "ModelStorage")
    private ModelStorage modelStorage;

    @XmlElement(name = "DataStorage")
    private DataStorage dataStorage;

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    public void setDataStorage(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    public ModelStorage getModelStorage() {
        if (modelStorage == null) {
            modelStorage = new ModelStorage();
            modelStorage.setStorageType("file");
            File f = new File(System.getProperty("carbon.home") + File.separator + "models");
            try {
                if (f.mkdir()) {
                    modelStorage.setStorageDirectory(f.getAbsolutePath());
                } else {
                    modelStorage.setStorageDirectory(System.getProperty("carbon.home") + File.separator + "tmp");
                }
            } catch (Exception e) {
                // can't create the directory, use an existing one.
                modelStorage.setStorageDirectory(System.getProperty("carbon.home") + File.separator + "tmp");
            }
        }
        return modelStorage;
    }

    public void setModelStorage(ModelStorage modelStorage) {
        this.modelStorage = modelStorage;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public List<MLAlgorithm> getMlAlgorithms() {
        return mlAlgorithms;
    }

    public void setMlAlgorithms(List<MLAlgorithm> mlAlgorithms) {
        this.mlAlgorithms = mlAlgorithms;
    }

    public SummaryStatisticsSettings getSummaryStatisticsSettings() {
        return summaryStatisticsSettings;
    }

    public void setSummaryStatisticsSettings(SummaryStatisticsSettings summaryStatisticsSettings) {
        this.summaryStatisticsSettings = summaryStatisticsSettings;
    }

    public List<MLProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<MLProperty> properties) {
        this.properties = properties;
    }

    public String getHdfsUrl() {
        return hdfsUrl;
    }

    public void setHdfsUrl(String hdfsUrl) {
        this.hdfsUrl = hdfsUrl;
    }

    public String getEmailNotificationEndpoint() {
        return emailNotificationEndpoint;
    }

    public String getModelRegistryLocation() {
        return modelRegistryLocation;
    }

    public void setModelRegistryLocation(String modelRegistryLocation) {
        this.modelRegistryLocation = modelRegistryLocation;
    }

    public void setEmailNotificationEndpoint(String emailNotificationEndpoint) {
        this.emailNotificationEndpoint = emailNotificationEndpoint;
    }
}
