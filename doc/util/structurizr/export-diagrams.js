const puppeteer = require('puppeteer');
const fs = require('fs');

const PNG_FORMAT = 'png';

const IMAGE_VIEW_TYPE = 'Image';

const url = process.argv[2];
const format = PNG_FORMAT;

var expectedNumberOfExports = 0;
var actualNumberOfExports = 0;

(async () => {
  const browser = await puppeteer.launch({
    executablePath: '/usr/bin/google-chrome-stable',
    ignoreHTTPSErrors: true,
    headless: 'shell',
    args: ['--enable-gpu', '--no-sandbox']
  });
  const page = await browser.newPage();

  // visit the diagrams page
  console.log(" - Opening " + url);
  await page.goto(url, { waitUntil: 'domcontentloaded' });
  await page.waitForFunction('structurizr.scripting && structurizr.scripting.isDiagramRendered() === true');

  // add a function to the page to save the generated PNG images
  await page.exposeFunction('savePNG', (content, filename) => {
    console.log(" - " + filename);
    content = content.replace(/^data:image\/png;base64,/, "");
    fs.writeFile(filename, content, 'base64', function (err) {
      if (err) throw err;
    });

    actualNumberOfExports++;

    if (actualNumberOfExports === expectedNumberOfExports) {
      console.log(" - Finished");
      browser.close();
    }
  });

  // get the array of views
  const views = await page.evaluate(() => {
    return structurizr.scripting.getViews();
  });

  views.forEach(function(view) {
    if (view.type === IMAGE_VIEW_TYPE) {
      expectedNumberOfExports++; // diagram only
    } else {
      expectedNumberOfExports++; // diagram
      expectedNumberOfExports++; // key
    }
  });

  console.log(" - Starting export");
  for (var i = 0; i < views.length; i++) {
    const view = views[i];

    await page.evaluate((view) => {
      structurizr.scripting.changeView(view.key);
    }, view);

    await page.waitForFunction('structurizr.scripting.isDiagramRendered() === true');

    const diagramFilename = './export/' + view.key + '.png';
    const diagramKeyFilename = './export/' + view.key + '-key.png'

    page.evaluate((diagramFilename) => {
      structurizr.scripting.exportCurrentDiagramToPNG({ includeMetadata: true, crop: false }, function(png) {
        window.savePNG(png, diagramFilename);
      })
    }, diagramFilename);

    if (view.type !== IMAGE_VIEW_TYPE) {
      page.evaluate((diagramKeyFilename) => {
        structurizr.scripting.exportCurrentDiagramKeyToPNG(function(png) {
          window.savePNG(png, diagramKeyFilename);
        })
      }, diagramKeyFilename);
    }
  }

})();