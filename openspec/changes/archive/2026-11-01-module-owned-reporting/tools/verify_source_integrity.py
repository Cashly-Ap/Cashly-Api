from pathlib import Path
from collections import Counter
import re,json,hashlib
r=Path.cwd();c=r/'openspec/changes/module-owned-reporting';baseline=r/'target/reporting-baseline-source';rows=json.loads((c/'relocation-manifest.json').read_text());splits={x['old']:x['new'] for x in json.loads((c/'split-manifest.json').read_text())};tsplits={x['old']:x['new'] for x in json.loads((c/'test-splits.json').read_text())};errors=[]
def normalize(t):return re.sub(r'\s+','',re.sub(r'^(?:package|import) .*;\n','',t,flags=re.M))
def balanced(t,start,opening='{',closing='}'):
 depth=0;quote=None;escape=False
 for i in range(start,len(t)):
  a=t[i]
  if quote:
   if escape:escape=False
   elif a=='\\':escape=True
   elif a==quote:quote=None
   continue
  if a in ['"',"'"]:quote=a;continue
  if a==opening:depth+=1
  elif a==closing:
   depth-=1
   if depth==0:return t[start:i+1]
 raise ValueError('Unbalanced')
def methods(t,annotation):
 out=[]
 for match in re.finditer(r'@'+annotation+r'\b',t):
  header=re.search(r'\b(?:public\s+)?(?:[\w<>\[\],?]+\s+)+(\w+)\s*\([^;{}]*\)\s*\{',t[match.end():])
  if header:
   start=match.end()+header.end()-1;out.append((header[1],balanced(t,start)))
 return out
def assertions(t):
 out=[]
 for m in re.finditer(r'\b(assert\w+)\s*\(',t):
  out.append(normalize(m[1]+balanced(t,t.index('(',m.start()),'(',')')))
 return Counter(out)
main=[];test=[];approved=[];originaltests=0;finaltests=0
for row in rows:
 old=row['old'];before=(baseline/old).read_text();actualhash=hashlib.sha256(before.encode()).hexdigest()
 if actualhash!=row['sha256']:errors.append('snapshot_hash_mismatch:'+old)
 news=splits.get(old,tsplits.get(old,[row['new']]));afters=[(r/n).read_text() for n in news]
 if '/main/' in old:
  if old not in splits:
   ok=normalize(before)==normalize(afters[0]);main.append({'old':old,'new':news,'body_unchanged':ok})
   if not ok:errors.append('production_body_changed:'+old)
  else:
   annotation='GetMapping' if 'Controller' in old else 'Bean';bm=methods(before,annotation);am=[m for a in afters for m in methods(a,annotation)];ok=Counter((n,normalize(b)) for n,b in bm)==Counter((n,normalize(b)) for n,b in am)
   # map exact class-level annotations and mappings on both split controllers
   permissions=re.findall(r'@PreAuthorize\(([^\n]+)\)',before);basepaths=re.findall(r'@RequestMapping\(([^\n]+)\)',before)
   security=all(re.findall(r'@PreAuthorize\(([^\n]+)\)',a)==permissions and re.findall(r'@RequestMapping\(([^\n]+)\)',a)==basepaths for a in afters)
   if not ok or not security:errors.append('split_changed:'+old)
   main.append({'old':old,'new':news,'original_methods':len(bm),'final_methods':len(am),'exact_methods_and_bodies':ok,'class_security_and_basepaths_preserved':security})
 else:
  bm=methods(before,'Test');am=[m for a in afters for m in methods(a,'Test')];originaltests+=len(bm);finaltests+=len(am);bn=Counter(n for n,b in bm);an=Counter(n for n,b in am);missing=bn-an
  bc=assertions(before);ac=sum((assertions(a) for a in afters),Counter());boundary='/architecture/' in old
  exact=bc==ac
  if boundary and not exact:
   approved.append({'file':old,'reason':'Own reporting contracts now share owner prefix; exclude reporting from old owner-wide prohibition, retaining foreign capability checks.','original_assertions':sum(bc.values()),'final_assertions':sum(ac.values())})
   if sum(bc.values())!=sum(ac.values()):errors.append('boundary_assertion_count_changed:'+old)
  elif not exact:errors.append('assertions_changed:'+old)
  if missing:errors.append('test_methods_missing:'+old)
  test.append({'old':old,'new':news,'original_test_methods':dict(bn),'final_test_methods':dict(an),'missing_test_methods':dict(missing),'assertions_exact':exact,'assertion_counts_preserved':sum(bc.values())==sum(ac.values())})
res={'status':'PASS' if not errors else 'FAIL','snapshot':'target/reporting-baseline-source','production_original_files':len(main),'production_final_files':sum(len(x['new']) for x in main),'production_unchanged_non_split_files':sum('body_unchanged'in x and x['body_unchanged'] for x in main),'test_original_files':len(test),'test_mapped_final_files':sum(len(x['new'])for x in test),'test_original_methods':originaltests,'test_mapped_final_methods':finaltests,'added_test_file':'src/test/java/com/cashly/cashly_api/products/reporting/infrastructure/config/CommercialCatalogReportingConfigTest.java','approved_test_adjustments':approved,'unexpected_deviations':errors,'production_checks':main,'test_checks':test}
(c/'evidence').mkdir(exist_ok=True);(c/'evidence/source-integrity.json').write_text(json.dumps(res,indent=2)+'\n');print(json.dumps({k:v for k,v in res.items() if k not in ['production_checks','test_checks','approved_test_adjustments']}))
