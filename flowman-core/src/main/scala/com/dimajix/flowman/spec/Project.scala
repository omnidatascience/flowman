/*
 * Copyright 2018 Kaya Kupferschmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimajix.flowman.spec

import java.io.File

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory

import com.dimajix.flowman.spec.flow.Mapping
import com.dimajix.flowman.spec.model.Relation
import com.dimajix.flowman.spec.output.Output
import com.dimajix.flowman.spec.runner.Runner
import com.dimajix.flowman.spec.task.Job


object Project {
    class Reader {
        private val logger = LoggerFactory.getLogger(classOf[Reader])

        /**
          * Loads a project file and all related module files
          *
          * @param file
          * @return
          */
        def file(file:File) : Project = {
            if (file.isDirectory) {
                logger.info(s"Reading project in directory ${file.toString}")
                this.file(new File(file, "project.yml"))
            }
            else {
                logger.info(s"Reading project file ${file.toString}")
                val project = ObjectMapper.read[Project](file)
                loadModules(project, file.getParentFile)
                project
            }
        }

        /**
          * Loads a project file
          *
          * @param filename
          * @return
          */
        def file(filename:String) : Project = {
            file(new File(filename))
        }

        def string(text:String) : Project = {
            ObjectMapper.parse[Project](text)
        }

        private def loadModules(project: Project, directory:File) : Unit = {
            val module = project.modules
                .map(f => Module.read.file(new File(directory, f)))
                .reduce((l,r) => l.merge(r))

            project._environment = module.environment
            project._config = module.config
            project._profiles = module.profiles
            project._connections = module.connections
            project._relations = module.relations
            project._mappings = module.mappings
            project._outputs = module.outputs
            project._jobs = module.jobs
        }
    }

    class Builder {
        private val project = new Project

        def build() : Project = project

        def setName(name:String) : Builder = {
            project._name = name
            this
        }

        def setEnvironment(env:Seq[(String,String)]) : Builder = {
            project._environment = env
            this
        }
        def setConfig(conf:Seq[(String,String)]) : Builder = {
            project._config = conf
            this
        }
        def setProfiles(profiles:Map[String,Profile]) : Builder = {
            project._profiles = profiles
            this
        }
        def addProfile(name:String, profile:Profile) : Builder = {
            project._profiles = project._profiles + (name -> profile)
            this
        }
        def setConnections(connections:Map[String,Connection]) : Builder = {
            project._connections = connections
            this
        }
        def addConnection(name:String, connection:Connection) : Builder = {
            project._connections = project._connections + (name -> connection)
            this
        }
        def setRelations(relations:Map[String,Relation]) : Builder = {
            project._relations = relations
            this
        }
        def addRelations(name:String, relation:Relation) : Builder = {
            project._relations = project._relations + (name -> relation)
            this
        }
        def setMappings(mappings:Map[String,Mapping]) : Builder = {
            project._mappings = mappings
            this
        }
        def addMapping(name:String, mapping:Mapping) : Builder = {
            project._mappings = project._mappings + (name -> mapping)
            this
        }
        def setOutputs(outputs:Map[String,Output]) : Builder = {
            project._outputs = outputs
            this
        }
        def addOutput(name:String, output:Output) : Builder = {
            project._outputs = project._outputs + (name -> output)
            this
        }
        def setJobs(jobs:Map[String,Job]) : Builder = {
            project._jobs = jobs
            this
        }
        def addJob(name:String, job:Job) : Builder = {
            project._jobs = project._jobs + (name -> job)
            this
        }
    }

    def read = new Reader

    def builder() = new Builder
}


class Project {
    @JsonProperty(value="name") private var _name: String = _
    @JsonProperty(value="version") private var _version: String = _
    @JsonProperty(value="modules") private var _modules: Seq[String] = Seq()

    private var _basedir: File = new File("")
    private var _filename: File = new File("")

    private var _environment: Seq[(String,String)] = Seq()
    private var _config: Seq[(String,String)] = Seq()
    private var _profiles: Map[String,Profile] = Map()
    private var _connections: Map[String,Connection] = Map()
    private var _relations: Map[String,Relation] = Map()
    private var _mappings: Map[String,Mapping] = Map()
    private var _outputs: Map[String,Output] = Map()
    private var _jobs: Map[String,Job] = Map()
    private var _runner: Runner = _

    def name : String = _name
    def version : String = _version
    def modules : Seq[String] = _modules
    def filename : File = _filename
    def basedir : File = _basedir

    def config : Seq[(String,String)] = _config
    def environment : Seq[(String,String)] = _environment
    def runner : Runner = _runner

    def profiles : Map[String,Profile] = _profiles
    def relations : Map[String,Relation] = _relations
    def connections : Map[String,Connection] = _connections
    def mappings : Map[String,Mapping] = _mappings
    def outputs : Map[String,Output] = _outputs
    def jobs : Map[String,Job] = _jobs
}

