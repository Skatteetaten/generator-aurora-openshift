'use strict';
const path = require('path');

var Generator = require('yeoman-generator');

module.exports = class extends Generator {
  constructor(args, opts) {
    super(args, opts);

    var whoamiCmd = this.spawnCommandSync('whoami', { stdout: "pipe" });
    this.username = whoamiCmd.toString().replace("\n", "")
  }

  prompting() {
    return this.prompt([
        {
          type: 'string',
          name: 'baseName',
          message: 'Name:',
          default: path.basename(process.cwd())
        },
        {
          type: 'string',
          name: 'packageName',
          message: 'Package name:',
          default: 'no.skatteetaten.aurora',
          store: true
        },
        {
          type: 'string',
          name: 'description',
          message: 'Description:'
        },
        {
          type: 'confirm',
          name: 'oracle',
          message: 'Do you need an Oracle database name:',
          default: false
        },
        {
          type: 'string',
          name: 'dbName',
          message: 'Enter database name in Oracle, if empty will not use database:',
          "default": "db",
          when: function (answers) {
            return answers.oracle
          }
        },
        {
          type: 'confirm',
          name: 'dbExample',
          message: 'Include database example:',
          default: true,
          when: function (answers) {
            return answers.oracle
          }
        }, {
          type: 'confirm',
          name: 'spock',
          message: 'Use Spock?',
          default: true
        }, {
          type: 'string',
          name: 'maintainer',
          message: 'Maintainer:',
          default: this.user.git.name() + " <" + this.user.git.email() + ">",
          store: true
        }, {
          type: 'confirm',
          name: 'openshift',
          message: 'Deploy to Openshift?',
          default: true
        },
        {
          type: 'string',
          name: 'affiliation',
          message: 'Affiliation:',
          default: 'paas',
          store: true,
          when: function (answers) {
            return answers.openshift
          }
        },
        {
          type: 'string',
          name: 'namespace',
          message: 'Namespace:',
          default: username,
          store: true,
          when: function (answers) {
            return answers.openshift
          }
        }
      ]
    ).then((props) => {
      this.baseName = props.baseName;
      this.packageName = props.packageName;
      this.oracle = props.oracle;
      this.dbName = props.dbName;
      this.dbExample = props.dbExample;
      this.spock = props.spock;
      this.maintainer = props.maintainer;
      this.description = props.description;

    })


  }

  writing() {

    var packageFolder = this.packageName.replace(/\./g, '/');

    this.fs.copyTpl(
      this.templatePath('files/**/*'),
      this.destinationPath(""),
      this
    );


    this.fs.copyTpl(
      this.templatePath('packageFiles/src/main/java/**/*'),
      this.destinationPath('src/main/java/' + packageFolder),
      this
    );

    this.fs.copyTpl(
      this.templatePath('packageFiles/src/test/groovy/**/*'),
      this.destinationPath('src/test/groovy/' + packageFolder),
      this
    );


    if (this.dbExample) {
      this.fs.copyTpl(
        this.templatePath('examples/counter/files/**/*'),
        this.destinationPath(""),
        this
      );

      this.fs.copyTpl(
        this.templatePath('examples/counter/packageFiles/src/main/java/**/*'),
        this.destinationPath('src/main/java/' + packageFolder),
        this
      );
    }
  }

  install() {

    this.spawnCommandSync('git', ['init', '-q']);
    this.spawnCommandSync('git', ['checkout', '-b', 'dev', '-q']);
    this.spawnCommandSync('git', ['add', '--all']);
    this.spawnCommandSync('git', ['commit', '-m', '"initial commit from aurora-openshift generator"', '-q']);
    this.spawnCommandSync('mvn', ['clean', 'upload']);

    if(this.openshift) {
      this.log("call aoc setup and aoc deploy")
      //this.spawnCommandSync('mvn', ['clean', 'upload']);

    }
  }

};