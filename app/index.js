'use strict';

var Generator = require('yeoman-generator');

module.exports = class extends Generator {
  constructor(args, opts) {
    super(args, opts);

    this.argument('name', { type: String, required: true, desc: 'The name of the application' });

    this.baseName=this.options.name

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
          type: 'string',
          name: 'maintainer',
          message: 'Maintainer:',
          default: this.user.git.name() + " <" + this.user.git.email() + ">"
        }
      ]
    ).then((props) => {
      props.baseName= this.baseName
      this.parameters=props

    })


  }

  writing() {

    var packageFolder = this.parameters.packageName.replace(/\./g, '/');

    this.config.set(this.parameters);

    this.fs.copyTpl(
      this.templatePath('files/**/*'),
      this.destinationPath(""), this.parameters);


    this.fs.copyTpl(
      this.templatePath('packageFiles/src/main/java/**/*'),
      this.destinationPath('src/main/java/' + packageFolder), this.parameters);

    if (this.parameters.spock) {
        this.fs.copyTpl(
            this.templatePath('packageFiles/src/test/groovy/**/*'),
            this.destinationPath('src/test/groovy/' + packageFolder), this.parameters);
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
    this.spawnCommandSync('git', ['checkout', '-b', 'yo-generator', '-q']);
    this.spawnCommandSync('git', ['add', '--all']);
    this.spawnCommandSync('git', ['commit', '-m', '"initial commit from aurora-openshift generator"', '-q']);
    this.spawnCommandSync('mvn', ['clean', 'deploy']);
  }

};