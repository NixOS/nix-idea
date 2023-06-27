"use strict";

module.exports = async ({github, context, core, outputFile, releaseTagInput, allowFallbackToLatest}) => {
  const { RELEASE_TAG_INPUT, ALLOW_FALLBACK_TO_LATEST } = process.env;
  const fs = require('fs');
  const owner = context.repo.owner;
  const repo = context.repo.repo;

  if (!outputFile) {
    throw new Error("outputFile not specified");
  }

  let releaseNote;
  if (context.eventName === 'release') {
    releaseNote = context.payload.release.body;
  }
  else if (releaseTagInput && context.payload.inputs[releaseTagInput]) {
    const tag = context.payload.inputs[releaseTagInput];
    try {
      const response = await github.rest.repos.getReleaseByTag({owner, repo, tag});
      releaseNote = response.data.body;
    }
    catch (e) {
      if (e.status === 404) {
        core.setFailed(`No release with tag '${tag}' has been found.`);
        return;
      }
      else {
        throw e;
      }
    }
  }
  else if (allowFallbackToLatest) {
    const response = await github.rest.repos.getLatestRelease({owner, repo});
    releaseNote = response.data.body;
  }
  else {
    throw new Error("No release tag specified and allowFallbackToLatest not set");
  }

  core.info(`Release notes:\n${releaseNote}`);
  fs.writeFileSync(outputFile, releaseNote, { encoding: 'utf8', flag: 'wx' });
}
