'use strict';

var Generator = require('yeoman-generator');

module.exports = class extends Generator {
  constructor(args, opts) {
    super(args, opts);

    this.argument('name', { type: String, required: true, desc: 'The name of the application' });

    this.baseName = this.options.name

  }


  prompting() {
    return this.prompt([
        {
          type: 'string',
          name: 'packageName',
          message: 'Package name:',
          default: 'no.skatteetaten.aurora.demo'
        },
        {
          type: 'string',
          name: 'description',
          message: 'Description:'
        },
        {
          type: 'confirm',
          name: 'oracle',
          message: 'Do you need an Oracle database name?',
          default: false
        },
        {
          type: 'string',
          name: 'dbName',
          message: 'Enter database name in Oracle, if empty will not use database:',
          "default": "db",
          when: function (answers) {
            return answers.oracle;
          }
        },
        {
          type: 'confirm',
          name: 'reactive',
          message: 'Do you want to use Spring Cloud Stream with Kafka?',
          default: false
        },
        {
          type: 'string',
          name: 'topicWrite',
          message: 'Enter name of kafka topic you want to write to:',
          when: function (answers) {
            return answers.reactive;
          }
        },
        {
          type: 'confirm',
          name: 'reactiveHttp',
          message: 'Do you want to write to kafka topic from HTTP?',
          default:false,
          when: function (answers) {
            return answers.topicWrite !== "";
          }
        },
        {
          type: 'string',
          name: 'topicRead',
          message: 'Enter name of kafka topic you want to read from:',
          when: function (answers) {
            return answers.reactive && !answers.reactiveHttp;
          }
        },
        {
          type: 'string',
          name: 'consumerGroup',
          message: 'Enter name of default kafka consumer group:',
          default: this.baseName,
          when: function (answers) {
            return answers.reactive;
          }
        },
        {
          type: 'string',
          name: 'kafka',
          message: 'Where is kafka exposed?',
          default: "kafka",
          when: function (answers) {
            return answers.reactive;
          }
        },
        {
          type: 'string',
          name: 'zookeeper',
          message: 'Where is zookeeper exposed?',
          default: "zookeeper",
          when: function (answers) {
            return answers.reactive;
          }
        },
        {
          type: 'confirm',
          name: 'dbExample',
          message: 'Include database example?',
          default: true,
          when: function (answers) {
            return answers.oracle;
          }
        }, {
          type: 'confirm',
          name: 'spock',
          message: 'Use Spock?',
          default: true
        }, {
          type: 'confirm',
          name: 'controllerExample',
          message: 'Do you want an example HTTP controller:',
          default: false,
        },
        {
          type: 'string',
          name: 'maintainer',
          message: 'Maintainer:',
          default: this.user.git.name() + " <" + this.user.git.email() + ">"
        }
      ]
    ).then((props) => {

      props.kafkaSink = false;
      props.kafkaSource = false;
      if (props.topicWrite !== "") {
        props.kafkaSink = true;
      }

      if (!(!props.topicRead)) {
        props.kafkaSource = true;
      }

      props.baseName = this.baseName
      this.parameters = props


    })


  }

  writing() {

    let packageFolder = this.parameters.packageName.replace(/\./g, '/');

    this.config.set(this.parameters);
    this.parameters.dump=this.parameters

    this.fs.copyTpl(
      this.templatePath('files/**/*'),
      this.destinationPath(""), this.parameters);

    this.fs.copyTpl(this.templatePath('files/.gitignore'), this.destinationPath('.gitignore'));

    this.fs.copyTpl(
      this.templatePath('packageFiles/src/main/java/**/*'),
      this.destinationPath('src/main/java/' + packageFolder), this.parameters);

    if (this.parameters.spock) {
      this.fs.copyTpl(
        this.templatePath('packageFiles/src/test/groovy/**/*'),
        this.destinationPath('src/test/groovy/' + packageFolder), this.parameters);
    }

    if (this.parameters.controllerExample) {
      this.fs.copyTpl(
        this.templatePath('examples/controller/packageFiles/src/main/java/**/*'),
        this.destinationPath('src/main/java/' + packageFolder), this.parameters);
    }

    if(this.parameters.reactive){

      let kafkaExampleFolder;
      if(this.parameters.reactiveHttp) {
        kafkaExampleFolder="kafkaHttpSink"
      } else if(this.parameters.kafkaSource && this.parameters.kafkaSink) {
        kafkaExampleFolder = "kafkaProcessor"
      } else if(this.parameters.kafkaSource) {
        kafkaExampleFolder = "kafkaSource"
      } else if(this.parameters.kafkaSink) {
        kafkaExampleFolder = "kafkaSink"
      }

      if(kafkaExampleFolder) {
        this.fs.copyTpl(
          this.templatePath('examples/' + kafkaExampleFolder + '/src/main/java/**/*'),
          this.destinationPath('src/main/java/' + packageFolder), this.parameters);
      }
    }

    if (this.parameters.dbExample) {
      this.fs.copyTpl(
        this.templatePath('examples/counter/files/**/*'),
        this.destinationPath(""), this.parameters);

      this.fs.copyTpl(
        this.templatePath('examples/counter/packageFiles/src/main/java/**/*'),
        this.destinationPath('src/main/java/' + packageFolder), this.parameters);
    }
  }

  install() {

    this.spawnCommandSync('git', ['init', '-q']);
    this.spawnCommandSync('git', ['checkout', '-b', 'dev', '-q']);
    this.spawnCommandSync('git', ['add', '--all']);
    this.spawnCommandSync('git', ['commit', '-m', '"initial commit from aurora-openshift generator"', '-q']);
    this.spawnCommandSync('mvn', ['clean', 'deploy']);
  }

};